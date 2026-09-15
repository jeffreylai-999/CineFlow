package com.cineflow.catalog;

import java.time.Instant;

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
		Instant sourceRefreshedAt) {
}
