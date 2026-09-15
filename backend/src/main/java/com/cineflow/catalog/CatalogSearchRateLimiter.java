package com.cineflow.catalog;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
class CatalogSearchRateLimiter {

	static final int MAX_KEYS = 10_000;

	private final ConcurrentHashMap<String, List<Instant>> attempts = new ConcurrentHashMap<>();
	private final Object admission = new Object();
	private final CatalogProperties catalogProperties;
	private final Clock clock;
	private Instant nextEviction;

	CatalogSearchRateLimiter(CatalogProperties catalogProperties, Clock clock) {
		this.catalogProperties = catalogProperties;
		this.clock = clock;
	}

	void check(String clientKey) {
		Instant now = clock.instant();
		Instant windowStart = now.minus(catalogProperties.rateLimitWindow());
		synchronized (admission) {
			if (!attempts.containsKey(clientKey) && attempts.size() >= MAX_KEYS) {
				if (nextEviction == null || !now.isBefore(nextEviction)) {
					evictExpired(windowStart);
				}
			}
			if (!attempts.containsKey(clientKey) && attempts.size() >= MAX_KEYS) {
				throw new CatalogRateLimitException(catalogProperties.rateLimitWindow());
			}
			recordAttempt(clientKey, now, windowStart);
		}
	}

	private void recordAttempt(String key, Instant now, Instant windowStart) {
		int limit = catalogProperties.searchRateLimit();
		attempts.compute(key, (ignored, existing) -> {
			List<Instant> stamps = existing == null ? new ArrayList<>() : existing;
			stamps.removeIf(instant -> instant.isBefore(windowStart));
			if (stamps.size() >= limit) {
				throw new CatalogRateLimitException(catalogProperties.rateLimitWindow());
			}
			stamps.add(now);
			noteEviction(now.plus(catalogProperties.rateLimitWindow()));
			return stamps;
		});
	}

	private void evictExpired(Instant windowStart) {
		Duration window = catalogProperties.rateLimitWindow();
		for (String key : attempts.keySet()) {
			attempts.computeIfPresent(key, (ignored, stamps) -> {
				stamps.removeIf(instant -> instant.isBefore(windowStart));
				return stamps.isEmpty() ? null : stamps;
			});
		}
		Instant earliest = null;
		for (List<Instant> stamps : attempts.values()) {
			for (Instant stamp : stamps) {
				Instant expires = stamp.plus(window);
				if (earliest == null || expires.isBefore(earliest)) {
					earliest = expires;
				}
			}
		}
		nextEviction = earliest;
	}

	private void noteEviction(Instant expires) {
		if (nextEviction == null || expires.isBefore(nextEviction)) {
			nextEviction = expires;
		}
	}
}
