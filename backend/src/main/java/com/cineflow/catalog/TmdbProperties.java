package com.cineflow.catalog;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cineflow.tmdb")
public record TmdbProperties(String accessToken, String baseUrl, String imageBaseUrl) {

	boolean configured() {
		return accessToken != null && !accessToken.isBlank();
	}
}
