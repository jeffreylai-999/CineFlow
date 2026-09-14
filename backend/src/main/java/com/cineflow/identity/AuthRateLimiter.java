package com.cineflow.identity;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
class AuthRateLimiter {

	static final int MAX_KEYS = 10_000;

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
		if (!attempts.containsKey(key) && attempts.size() >= MAX_KEYS) {
			evictExpired(windowStart);
			if (!attempts.containsKey(key) && attempts.size() >= MAX_KEYS) {
				throw new RateLimitException(authProperties.rateLimitWindow());
			}
		}
		attempts.compute(key, (ignored, existing) -> {
			List<Instant> stamps = existing == null ? new ArrayList<>() : existing;
			stamps.removeIf(instant -> instant.isBefore(windowStart));
			if (stamps.size() >= limit) {
				throw new RateLimitException(authProperties.rateLimitWindow());
			}
			stamps.add(now);
			return stamps;
		});
	}

	private void evictExpired(Instant windowStart) {
		for (String key : attempts.keySet()) {
			attempts.computeIfPresent(key, (ignored, stamps) -> {
				stamps.removeIf(instant -> instant.isBefore(windowStart));
				return stamps.isEmpty() ? null : stamps;
			});
		}
	}
}
