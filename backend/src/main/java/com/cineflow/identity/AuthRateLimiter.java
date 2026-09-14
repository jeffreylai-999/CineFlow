package com.cineflow.identity;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

@Component
class AuthRateLimiter {

	private final ConcurrentHashMap<String, List<Instant>> attempts = new ConcurrentHashMap<>();
	private final AuthProperties authProperties;
	private final Clock clock;

	AuthRateLimiter(AuthProperties authProperties, Clock clock) {
		this.authProperties = authProperties;
		this.clock = clock;
	}

	void checkLogin(String clientKey) {
		check("login:" + clientKey, authProperties.loginRateLimit());
	}

	void checkRefresh(String clientKey) {
		check("refresh:" + clientKey, authProperties.refreshRateLimit());
	}

	private void check(String key, int limit) {
		Instant now = clock.instant();
		Instant windowStart = now.minus(authProperties.rateLimitWindow());
		List<Instant> stamps = attempts.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>());
		stamps.removeIf(instant -> instant.isBefore(windowStart));
		if (stamps.size() >= limit) {
			throw new RateLimitException(authProperties.rateLimitWindow());
		}
		stamps.add(now);
	}
}
