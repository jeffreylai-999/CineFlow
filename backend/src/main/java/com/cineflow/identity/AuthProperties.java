package com.cineflow.identity;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cineflow.auth")
public record AuthProperties(
		Duration accessTokenTtl,
		Duration refreshTokenTtl,
		Cookie cookie,
		int loginRateLimit,
		int refreshRateLimit,
		Duration rateLimitWindow,
		Jwt jwt) {

	public record Cookie(String name, boolean secure, String sameSite, String path) {
	}

	public record Jwt(String secret) {
	}
}
