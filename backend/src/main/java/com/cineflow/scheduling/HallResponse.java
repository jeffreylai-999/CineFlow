package com.cineflow.scheduling;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HallResponse(
		long id,
		String name,
		int rowCount,
		int seatsPerRow,
		Instant archivedAt,
		List<SeatResponse> seats) {
}
