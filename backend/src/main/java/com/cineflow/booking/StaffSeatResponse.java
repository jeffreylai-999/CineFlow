package com.cineflow.booking;

public record StaffSeatResponse(
		long id,
		String rowLabel,
		int seatNumber,
		String label,
		StaffSeatState state) {
}
