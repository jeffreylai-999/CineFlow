package com.cineflow.scheduling;

import java.math.BigDecimal;
import java.time.Instant;

public record ShowtimeResponse(
		long id,
		long movieId,
		String movieTitle,
		int runtimeMinutes,
		long hallId,
		String hallName,
		Instant startsAt,
		String startsAtCinemaTime,
		String timeZone,
		Instant occupancyEndsAt,
		BigDecimal adultPriceMyr,
		BigDecimal childPriceMyr) {
}
