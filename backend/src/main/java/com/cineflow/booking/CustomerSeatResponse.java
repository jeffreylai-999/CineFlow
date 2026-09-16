package com.cineflow.booking;

public record CustomerSeatResponse(
		long id,
		String rowLabel,
		int seatNumber,
		String label,
		boolean available) {
}
