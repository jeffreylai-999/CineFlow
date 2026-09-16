package com.cineflow.booking;

import java.math.BigDecimal;
import java.util.List;

public record CustomerSeatMapResponse(
		long showtimeId,
		long movieId,
		String movieTitle,
		String hallName,
		String startsAtCinemaTime,
		String timeZone,
		BigDecimal adultPriceMyr,
		BigDecimal childPriceMyr,
		int bookingLimit,
		List<CustomerSeatResponse> seats) {
}
