package com.cineflow.catalog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImportMovieRequest(
		@NotBlank String providerId,
		@NotBlank String externalId,
		@Min(1) @Max(600) Integer runtimeMinutes,
		@Size(max = 16) String ageRating) {
}
