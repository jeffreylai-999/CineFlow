package com.cineflow.catalog;

import jakarta.validation.constraints.NotBlank;

public record SelectMovieProviderRequest(@NotBlank String providerId) {
}
