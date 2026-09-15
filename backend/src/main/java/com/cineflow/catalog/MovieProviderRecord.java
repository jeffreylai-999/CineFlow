package com.cineflow.catalog;

public record MovieProviderRecord(
		String providerId,
		String externalId,
		String title,
		String synopsis,
		String genre,
		String posterUrl,
		Integer runtimeMinutes,
		String ageRating) {
}
