package com.cineflow.scheduling;

public record SeatResponse(long id, String rowLabel, int seatNumber, String label, boolean disabled) {
}
