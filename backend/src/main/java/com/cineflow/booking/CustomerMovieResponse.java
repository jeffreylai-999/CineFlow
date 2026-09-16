package com.cineflow.booking;

import java.util.List;

public record CustomerMovieResponse(
		long id,
		String title,
		String synopsis,
		String genre,
		int runtimeMinutes,
		String ageRating,
		String posterUrl,
		List<CustomerShowtimeDateGroup> dates) {
}
