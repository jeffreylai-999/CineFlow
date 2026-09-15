package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cineflow.audit.Audit;
import com.cineflow.audit.AuditAction;

class MovieMetadataProvidersTest {

	private MovieMetadataProvider tmdb;
	private MovieMetadataProvider omdb;
	private CatalogSettingsRepository settings;
	private Audit audit;
	private MovieMetadataProviders providers;

	@BeforeEach
	void createProviders() {
		tmdb = mock(MovieMetadataProvider.class);
		omdb = mock(MovieMetadataProvider.class);
		settings = mock(CatalogSettingsRepository.class);
		audit = mock(Audit.class);
		when(tmdb.providerId()).thenReturn("tmdb");
		when(omdb.providerId()).thenReturn("omdb");
		CatalogSettingsEntity row = new CatalogSettingsEntity();
		row.setActiveProvider("tmdb");
		when(settings.findById(1)).thenReturn(Optional.of(row));
		providers = new MovieMetadataProviders(
				tmdb,
				omdb,
				new TmdbProperties("token", "https://api.themoviedb.org/3", "https://image.tmdb.org/t/p/w500", null, null),
				new OmdbProperties("", "https://www.omdbapi.com", null, null),
				settings,
				audit);
	}

	@Test
	void configuredListOmitsProvidersWithoutCredentials() {
		assertThat(providers.configured()).containsExactly(new MovieProviderOption("tmdb", "TMDB"));
	}

	@Test
	void selectRejectsAnUnconfiguredProviderWithoutWriting() {
		assertThatThrownBy(() -> providers.select(2L, "omdb"))
			.isInstanceOf(CatalogException.class)
			.extracting(error -> ((CatalogException) error).code())
			.isEqualTo("catalog.provider_not_configured");
		verify(settings, never()).save(org.mockito.ArgumentMatchers.any());
		verify(audit, never()).record(
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.eq(AuditAction.MOVIE_PROVIDER_SELECTED),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any());
	}

	@Test
	void sourceLooksUpTheOriginalAdapterWithoutUsingTheActiveSelection() {
		assertThat(providers.source("omdb")).isSameAs(omdb);
		assertThat(providers.active()).isSameAs(tmdb);
	}

	@Test
	void sourceRejectsAnUnknownProviderAsInvalidRatherThanAnOutage() {
		assertThatThrownBy(() -> providers.source("fixture"))
			.isInstanceOf(CatalogException.class)
			.extracting(error -> ((CatalogException) error).code())
			.isEqualTo("catalog.invalid_request");
	}

	@Test
	void currentKeepsStoredTmdbListedWhenOnlyOmdbIsConfigured() {
		providers = new MovieMetadataProviders(
				tmdb,
				omdb,
				new TmdbProperties("", "https://api.themoviedb.org/3", "https://image.tmdb.org/t/p/w500", null, null),
				new OmdbProperties("omdb-key", "https://www.omdbapi.com", null, null),
				settings,
				audit);

		MovieProviderSettingsResponse current = providers.current();

		assertThat(current.activeProviderId()).isEqualTo("tmdb");
		assertThat(current.providers()).containsExactly(
				new MovieProviderOption("tmdb", "TMDB"),
				new MovieProviderOption("omdb", "OMDb"));
		assertThatThrownBy(() -> providers.active())
			.isInstanceOf(CatalogException.class)
			.extracting(error -> ((CatalogException) error).code())
			.isEqualTo("catalog.provider_not_configured");
		assertThatThrownBy(() -> providers.select(2L, "tmdb"))
			.isInstanceOf(CatalogException.class)
			.extracting(error -> ((CatalogException) error).code())
			.isEqualTo("catalog.provider_not_configured");
		verify(settings, never()).save(org.mockito.ArgumentMatchers.any());
	}
}
