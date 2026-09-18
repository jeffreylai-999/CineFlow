package com.cineflow.catalog;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

public record MovieAdminResponse(
		long id,
		String title,
		String synopsis,
		String genre,
		int runtimeMinutes,
		String ageRating,
		String posterUrl,
		String sourceProvider,
		String externalId,
		Instant sourceRefreshedAt,
		@JsonInclude(JsonInclude.Include.NON_NULL) Instant archivedAt,
		boolean providerRetentionWarning,
		@JsonInclude(JsonInclude.Include.NON_NULL) Instant providerRetentionExpiresAt) {
}
