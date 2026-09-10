package com.cineflow.catalog;

public record MovieResponse(
		long id,
		String title,
		String synopsis,
		String genre,
		int runtimeMinutes,
		String ageRating,
		String posterUrl) {
}
