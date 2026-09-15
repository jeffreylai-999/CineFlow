package com.cineflow.scheduling;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HallSummaryResponse(
		long id, String name, int rowCount, int seatsPerRow, Instant archivedAt) {
}
