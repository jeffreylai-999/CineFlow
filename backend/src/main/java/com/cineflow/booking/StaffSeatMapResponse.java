package com.cineflow.booking;

import java.math.BigDecimal;
import java.util.List;

public record StaffSeatMapResponse(
		long showtimeId,
		String movieTitle,
		String hallName,
		String startsAtCinemaTime,
		String timeZone,
		BigDecimal adultPriceMyr,
		BigDecimal childPriceMyr,
		int bookingLimit,
		boolean counterSalesOpen,
		List<StaffSeatResponse> seats) {
}
