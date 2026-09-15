package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.cineflow.audit.Audit;

class CatalogAdministrationServiceTest {

	private MovieMetadataProvider provider;
	private MovieRepository movieRepository;
	private CatalogAdministrationService service;

	@BeforeEach
	void createService() {
		provider = mock(MovieMetadataProvider.class);
		movieRepository = mock(MovieRepository.class);
		when(provider.providerId()).thenReturn("tmdb");
		service = new CatalogAdministrationService(
				provider,
				movieRepository,
				mock(Audit.class),
				Clock.fixed(Instant.parse("2026-09-15T00:00:00Z"), ZoneOffset.UTC),
				immediateTransactions());
	}

	@Test
	void importDoesNotCallTheProviderWhenTheIdentifierAlreadyExists() {
		when(movieRepository.findBySourceProviderAndExternalId("tmdb", "4242"))
				.thenReturn(Optional.of(existingMovie()));

		assertThatThrownBy(() -> service.importMovie(2L, "4242", null, null))
				.isInstanceOf(CatalogException.class)
				.extracting(error -> ((CatalogException) error).code())
				.isEqualTo("catalog.duplicate_import");
		verify(provider, never()).fetch(any());
	}

	@Test
	void importMapsOnlyTheProviderIdentityConstraintToDuplicate() {
		when(movieRepository.findBySourceProviderAndExternalId("tmdb", "4242")).thenReturn(Optional.empty());
		when(provider.fetch("4242")).thenReturn(providerRecord());
		when(movieRepository.saveAndFlush(any(MovieEntity.class))).thenThrow(new DataIntegrityViolationException(
				"ERROR: duplicate key value violates unique constraint \"movies_provider_external_unique\""));

		assertThatThrownBy(() -> service.importMovie(2L, "4242", null, null))
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

		assertThatThrownBy(() -> service.importMovie(2L, "4242", null, null))
				.isInstanceOf(CatalogException.class)
				.extracting(error -> ((CatalogException) error).code())
				.isEqualTo("catalog.save_failed");
	}

	@Test
	void providerIdentityConflictLooksAtTheConstraintName() {
		assertThat(CatalogAdministrationService.isProviderIdentityConflict(new DataIntegrityViolationException(
				"could not execute statement [movies_provider_external_unique]"))).isTrue();
		assertThat(CatalogAdministrationService.isProviderIdentityConflict(
				new DataIntegrityViolationException("could not execute statement [movies_runtime_check]"))).isFalse();
	}

	private static MovieEntity existingMovie() {
		return MovieEntity.imported(
				"The Courier Gate",
				"",
				"Adventure",
				118,
				"PG-13",
				null,
				"tmdb",
				"4242",
				Instant.parse("2026-09-15T00:00:00Z"));
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
