package com.cineflow.catalog;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cineflow.tmdb")
public record TmdbProperties(
		String accessToken,
		String baseUrl,
		String imageBaseUrl,
		Duration connectTimeout,
		Duration readTimeout) {

	private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(5);

	public TmdbProperties {
		if (connectTimeout == null || connectTimeout.isZero() || connectTimeout.isNegative()) {
			connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		}
		if (readTimeout == null || readTimeout.isZero() || readTimeout.isNegative()) {
			readTimeout = DEFAULT_READ_TIMEOUT;
		}
	}

	boolean configured() {
		return accessToken != null && !accessToken.isBlank();
	}
}
