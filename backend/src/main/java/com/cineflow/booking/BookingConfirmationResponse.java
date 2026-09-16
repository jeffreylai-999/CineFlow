package com.cineflow.booking;

import java.math.BigDecimal;
import java.util.List;

public record BookingConfirmationResponse(
		String bookingReference,
		long showtimeId,
		String movieTitle,
		String hallName,
		String startsAtCinemaTime,
		String timeZone,
		String email,
		List<BookedSeatResponse> seats,
		BigDecimal totalMyr,
		String admissionToken) {
}
