package com.cineflow.scheduling;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHallRequest(
		@NotBlank @Size(max = 80) String name,
		@Min(1) @Max(26) int rowCount,
		@Min(1) @Max(40) int seatsPerRow) {
}
