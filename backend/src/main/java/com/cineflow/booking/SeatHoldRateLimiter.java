package com.cineflow.booking;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
class SeatHoldRateLimiter {

	private static final int LIMIT = 10;
	private static final Duration WINDOW = Duration.ofMinutes(1);

	private final ConcurrentHashMap<String, List<Instant>> attempts = new ConcurrentHashMap<>();
	private final Clock clock;

	SeatHoldRateLimiter(Clock clock) {
		this.clock = clock;
	}

	void check(String clientAddress) {
		Instant now = clock.instant();
		Instant windowStart = now.minus(WINDOW);
		attempts.compute(clientAddress, (ignored, recorded) -> {
			List<Instant> current = recorded == null ? new ArrayList<>() : recorded;
			current.removeIf(attempt -> attempt.isBefore(windowStart));
			if (current.size() >= LIMIT) {
				throw BookingException.rateLimited(WINDOW);
			}
			current.add(now);
			return current;
		});
	}
}
