package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ProviderRetentionTest {

	private static final Instant NOW = Instant.parse("2026-09-16T12:00:00Z");

	@Test
	void warnsWithinThirtyDaysOfTheTmdbSixMonthLimit() {
		Instant refreshed = Instant.parse("2026-04-01T12:00:00Z");

		assertThat(ProviderRetention.expiresAt("tmdb", refreshed)).isEqualTo(Instant.parse("2026-09-28T12:00:00Z"));
		assertThat(ProviderRetention.warning("tmdb", refreshed, null, NOW)).isTrue();
	}

	@Test
	void doesNotWarnWhenTmdbDataWasRefreshedRecently() {
		Instant refreshed = Instant.parse("2026-08-01T12:00:00Z");

		assertThat(ProviderRetention.warning("tmdb", refreshed, null, NOW)).isFalse();
	}

	@Test
	void doesNotWarnForOmDbOrArchivedMovies() {
		Instant refreshed = Instant.parse("2025-01-01T12:00:00Z");

		assertThat(ProviderRetention.expiresAt("omdb", refreshed)).isNull();
		assertThat(ProviderRetention.warning("omdb", refreshed, null, NOW)).isFalse();
		assertThat(ProviderRetention.warning("tmdb", refreshed, NOW, NOW)).isFalse();
	}
}
