package com.cineflow.booking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
class BookingRateLimiter {

	private static final int LIMIT = 10;
	private static final int MAX_KEYS = 10_000;
	private static final Duration WINDOW = Duration.ofMinutes(1);

	private final ConcurrentHashMap<String, List<Instant>> attempts = new ConcurrentHashMap<>();
	private final Object admission = new Object();
	private final Clock clock;
	private Instant nextEviction;

	BookingRateLimiter(Clock clock) {
		this.clock = clock;
	}

	void checkSeatHold(String clientAddress) {
		check("seat-hold:" + clientAddress);
	}

	void checkCheckout(String clientAddress) {
		check("checkout:" + clientAddress);
	}

	private void check(String key) {
		Instant now = clock.instant();
		Instant windowStart = now.minus(WINDOW);
		synchronized (admission) {
			if (!attempts.containsKey(key) && attempts.size() >= MAX_KEYS) {
				if (nextEviction == null || !now.isBefore(nextEviction)) {
					evictExpired(windowStart);
				}
			}
			if (!attempts.containsKey(key) && attempts.size() >= MAX_KEYS) {
				throw BookingException.rateLimited(WINDOW);
			}
			recordAttempt(key, now, windowStart);
		}
	}

	private void recordAttempt(String key, Instant now, Instant windowStart) {
		attempts.compute(key, (ignored, recorded) -> {
			List<Instant> current = recorded == null ? new ArrayList<>() : recorded;
			current.removeIf(attempt -> attempt.isBefore(windowStart));
			if (current.size() >= LIMIT) {
				throw BookingException.rateLimited(WINDOW);
			}
			current.add(now);
			noteEviction(now.plus(WINDOW));
			return current;
		});
	}

	private void evictExpired(Instant windowStart) {
		for (String key : attempts.keySet()) {
			attempts.computeIfPresent(key, (ignored, recorded) -> {
				recorded.removeIf(attempt -> attempt.isBefore(windowStart));
				return recorded.isEmpty() ? null : recorded;
			});
		}
		nextEviction = attempts.values().stream()
			.flatMap(List::stream)
			.map(attempt -> attempt.plus(WINDOW))
			.min(Instant::compareTo)
			.orElse(null);
	}

	private void noteEviction(Instant expires) {
		if (nextEviction == null || expires.isBefore(nextEviction)) {
			nextEviction = expires;
		}
	}
}
