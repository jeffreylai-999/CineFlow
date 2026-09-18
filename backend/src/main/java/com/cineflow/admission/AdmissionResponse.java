package com.cineflow.admission;

import java.time.Instant;
import java.util.List;

public record AdmissionResponse(
		String bookingReference,
		String movieTitle,
		String hallName,
		String startsAtCinemaTime,
		String timeZone,
		List<AdmittedSeatResponse> seats,
		Instant admittedAt) {
}
