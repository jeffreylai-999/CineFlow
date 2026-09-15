package com.cineflow.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMovieRequest(
		@Min(1) @Max(600) int runtimeMinutes,
		@NotBlank @Size(max = 16) String ageRating) {
}
