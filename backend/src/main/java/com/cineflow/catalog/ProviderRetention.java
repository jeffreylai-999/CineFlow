package com.cineflow.catalog;

import java.time.Duration;
import java.time.Instant;

final class ProviderRetention {

	private static final Duration TMDB_RETENTION = Duration.ofDays(180);
	private static final Duration WARN_BEFORE = Duration.ofDays(30);

	private ProviderRetention() {
	}

	static Instant expiresAt(String sourceProvider, Instant sourceRefreshedAt) {
		if (!"tmdb".equals(sourceProvider) || sourceRefreshedAt == null) {
			return null;
		}
		return sourceRefreshedAt.plus(TMDB_RETENTION);
	}

	static boolean warning(String sourceProvider, Instant sourceRefreshedAt, Instant archivedAt, Instant now) {
		if (archivedAt != null) {
			return false;
		}
		Instant expiresAt = expiresAt(sourceProvider, sourceRefreshedAt);
		if (expiresAt == null) {
			return false;
		}
		return !now.isBefore(expiresAt.minus(WARN_BEFORE));
	}
}
