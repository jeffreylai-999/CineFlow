package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.cineflow.audit.Audit;

class CatalogAdministrationServiceTest {

	private MovieMetadataProvider provider;
	private MovieMetadataProviders providers;
	private MovieRepository movieRepository;
	private CatalogAdministrationService service;

	@BeforeEach
	void createService() {
		provider = mock(MovieMetadataProvider.class);
		providers = mock(MovieMetadataProviders.class);
		movieRepository = mock(MovieRepository.class);
		when(provider.providerId()).thenReturn("tmdb");
		when(providers.active()).thenReturn(provider);
		when(providers.activeProviderId()).thenReturn("tmdb");
		when(providers.requireActive("tmdb")).thenReturn(provider);
		when(providers.source("tmdb")).thenReturn(provider);
		service = new CatalogAdministrationService(
				providers,
				movieRepository,
				mock(JdbcTemplate.class),
				mock(Audit.class),
				Clock.fixed(Instant.parse("2026-09-15T00:00:00Z"), ZoneOffset.UTC),
				immediateTransactions());
	}

	@Test
	void importDoesNotCallTheProviderWhenTheIdentifierAlreadyExists() {
		when(movieRepository.findBySourceProviderAndExternalId("tmdb", "4242"))
				.thenReturn(Optional.of(existingMovie()));

		assertThatThrownBy(() -> service.importMovie(2L, "tmdb", "4242", null, null))
				.isInstanceOf(CatalogException.class)
				.extracting(error -> ((CatalogException) error).code())
				.isEqualTo("catalog.duplicate_import");
		verify(provider, never()).fetch(any());
	}

	@Test
	void importRejectsAHitWhenTheActiveProviderHasChanged() {
		when(providers.requireActive("tmdb")).thenThrow(CatalogException.providerMismatch());

		assertThatThrownBy(() -> service.importMovie(2L, "tmdb", "4242", null, null))
				.isInstanceOf(CatalogException.class)
				.extracting(error -> ((CatalogException) error).code())
				.isEqualTo("catalog.provider_mismatch");
		verify(provider, never()).fetch(any());
		verify(providers, never()).active();
	}

	@Test
	void importDoesNotPersistWhenTheActiveProviderChangesDuringFetch() {
		when(movieRepository.findBySourceProviderAndExternalId("tmdb", "4242")).thenReturn(Optional.empty());
		when(provider.fetch("4242")).thenReturn(providerRecord());
		doThrow(CatalogException.providerMismatch()).when(providers).requireStillActive("tmdb");

		assertThatThrownBy(() -> service.importMovie(2L, "tmdb", "4242", null, null))
				.isInstanceOf(CatalogException.class)
				.extracting(error -> ((CatalogException) error).code())
				.isEqualTo("catalog.provider_mismatch");
		verify(provider).fetch("4242");
		verify(movieRepository, never()).saveAndFlush(any());
	}

	@Test
	void importMapsOnlyTheProviderIdentityConstraintToDuplicate() {
		when(movieRepository.findBySourceProviderAndExternalId("tmdb", "4242")).thenReturn(Optional.empty());
		when(provider.fetch("4242")).thenReturn(providerRecord());
		when(movieRepository.saveAndFlush(any(MovieEntity.class))).thenThrow(new DataIntegrityViolationException(
				"ERROR: duplicate key value violates unique constraint \"movies_provider_external_unique\""));

		assertThatThrownBy(() -> service.importMovie(2L, "tmdb", "4242", null, null))
				.isInstanceOf(CatalogException.class)
				.extracting(error -> ((CatalogException) error).code())
				.isEqualTo("catalog.duplicate_import");
	}

	@Test
	void importDoesNotTreatOtherIntegrityFailuresAsDuplicates() {
		when(movieRepository.findBySourceProviderAndExternalId("tmdb", "4242")).thenReturn(Optional.empty());
		when(provider.fetch("4242")).thenReturn(providerRecord());
		when(movieRepository.saveAndFlush(any(MovieEntity.class))).thenThrow(
				new DataIntegrityViolationException("ERROR: new row violates check constraint \"movies_runtime_check\""));

		assertThatThrownBy(() -> service.importMovie(2L, "tmdb", "4242", null, null))
				.isInstanceOf(CatalogException.class)
				.extracting(error -> ((CatalogException) error).code())
				.isEqualTo("catalog.save_failed");
	}

	@Test
	void refreshUsesTheMovieSourceProviderInsteadOfTheActiveSelection() {
		MovieMetadataProvider omdb = mock(MovieMetadataProvider.class);
		when(omdb.providerId()).thenReturn("omdb");
		when(providers.active()).thenReturn(omdb);
		when(providers.source("tmdb")).thenReturn(provider);
		when(movieRepository.findById(8L)).thenReturn(Optional.of(existingMovie()));
		when(provider.fetch("4242")).thenReturn(providerRecord());

		MovieAdminResponse refreshed = service.refresh(2L, 8L);

		assertThat(refreshed.title()).isEqualTo("The Courier Gate");
		verify(provider).fetch("4242");
		verify(omdb, never()).fetch(any());
	}

	@Test
	void showtimeOccupancyConflictLooksAtTheConstraintName() {
		assertThat(CatalogAdministrationService.isShowtimeOccupancyConflict(new DataIntegrityViolationException(
				"could not execute statement [showtimes_hall_occupancy_excl]"))).isTrue();
		assertThat(CatalogAdministrationService.isShowtimeOccupancyConflict(
				new DataIntegrityViolationException("could not execute statement [movies_runtime_check]"))).isFalse();
	}

	@Test
	void providerIdentityConflictLooksAtTheConstraintName() {
		assertThat(CatalogAdministrationService.isProviderIdentityConflict(new DataIntegrityViolationException(
				"could not execute statement [movies_provider_external_unique]"))).isTrue();
		assertThat(CatalogAdministrationService.isProviderIdentityConflict(
				new DataIntegrityViolationException("could not execute statement [movies_runtime_check]"))).isFalse();
	}

	private static MovieEntity existingMovie() {
		MovieEntity movie = MovieEntity.imported(
				"The Courier Gate",
				"",
				"Adventure",
				118,
				"PG-13",
				null,
				"tmdb",
				"4242",
				Instant.parse("2026-09-15T00:00:00Z"));
		try {
			var id = MovieEntity.class.getDeclaredField("id");
			id.setAccessible(true);
			id.set(movie, 8L);
		}
		catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
		return movie;
	}

	private static MovieProviderRecord providerRecord() {
		return new MovieProviderRecord(
				"tmdb",
				"4242",
				"The Courier Gate",
				"A courier crew races a sealed cargo across three colonies.",
				"Adventure",
				"https://image.tmdb.org/t/p/w500/courier-gate.jpg",
				118,
				"PG-13");
	}

	private static PlatformTransactionManager immediateTransactions() {
		return new PlatformTransactionManager() {
			@Override
			public TransactionStatus getTransaction(TransactionDefinition definition) {
				return new SimpleTransactionStatus();
			}

			@Override
			public void commit(TransactionStatus status) {
			}

			@Override
			public void rollback(TransactionStatus status) {
			}
		};
	}
}
