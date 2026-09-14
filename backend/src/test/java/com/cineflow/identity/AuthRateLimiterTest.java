package com.cineflow.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.cineflow.MutableClock;

class AuthRateLimiterTest {

	private static final Instant START = Instant.parse("2026-09-14T00:00:00Z");

	@Test
	void rejectsABurstPastTheLimitWhenRequestsRace() throws Exception {
		MutableClock clock = new MutableClock(START);
		AuthRateLimiter limiter = new AuthRateLimiter(properties(5), clock);
		AtomicInteger allowed = new AtomicInteger();
		int threads = 20;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threads);
		try {
			for (int i = 0; i < threads; i++) {
				pool.submit(() -> {
					try {
						start.await();
						limiter.checkLogin("127.0.0.1:alice");
						allowed.incrementAndGet();
					}
					catch (InterruptedException interrupted) {
						Thread.currentThread().interrupt();
					}
					catch (RateLimitException ignored) {
					}
					finally {
						done.countDown();
					}
				});
			}
			start.countDown();
			done.await(5, TimeUnit.SECONDS);
		}
		finally {
			pool.shutdownNow();
		}
		assertThat(allowed.get()).isEqualTo(5);
	}

	@Test
	void evictsExpiredKeysSoANewClientCanBeTracked() {
		MutableClock clock = new MutableClock(START);
		AuthRateLimiter limiter = new AuthRateLimiter(properties(1), clock);
		for (int i = 0; i < AuthRateLimiter.MAX_KEYS; i++) {
			limiter.checkLogin("flood-" + i);
		}
		assertThatThrownBy(() -> limiter.checkLogin("overflow"))
			.isInstanceOf(RateLimitException.class);

		clock.set(START.plus(Duration.ofMinutes(2)));
		limiter.checkLogin("new-client");
	}

	@Test
	void evictionDoesNotThrowWhenOtherKeysAreUpdated() throws Exception {
		MutableClock clock = new MutableClock(START);
		AuthRateLimiter limiter = new AuthRateLimiter(properties(20), clock);
		for (int i = 0; i < AuthRateLimiter.MAX_KEYS; i++) {
			limiter.checkLogin("flood-" + i);
		}
		clock.set(START.plus(Duration.ofMinutes(2)));
		int threads = 32;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threads);
		AtomicInteger failures = new AtomicInteger();
		try {
			for (int i = 0; i < threads; i++) {
				int n = i;
				pool.submit(() -> {
					try {
						start.await();
						limiter.checkLogin(n < 16 ? "flood-" + n : "live-" + n);
					}
					catch (InterruptedException interrupted) {
						Thread.currentThread().interrupt();
						failures.incrementAndGet();
					}
					catch (RateLimitException ignored) {
					}
					catch (RuntimeException exception) {
						failures.incrementAndGet();
					}
					finally {
						done.countDown();
					}
				});
			}
			start.countDown();
			done.await(5, TimeUnit.SECONDS);
		}
		finally {
			pool.shutdownNow();
		}
		assertThat(failures.get()).isZero();
	}

	@Test
	void concurrentNewKeysDoNotExceedMaxKeys() throws Exception {
		MutableClock clock = new MutableClock(START);
		AuthRateLimiter limiter = new AuthRateLimiter(properties(100), clock);
		int total = AuthRateLimiter.MAX_KEYS + 200;
		AtomicInteger allowed = new AtomicInteger();
		ExecutorService pool = Executors.newFixedThreadPool(32);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(total);
		try {
			for (int i = 0; i < total; i++) {
				int n = i;
				pool.submit(() -> {
					try {
						start.await();
						limiter.checkLogin("flood-" + n);
						allowed.incrementAndGet();
					}
					catch (InterruptedException interrupted) {
						Thread.currentThread().interrupt();
					}
					catch (RateLimitException ignored) {
					}
					finally {
						done.countDown();
					}
				});
			}
			start.countDown();
			done.await(30, TimeUnit.SECONDS);
		}
		finally {
			pool.shutdownNow();
		}
		assertThat(allowed.get()).isEqualTo(AuthRateLimiter.MAX_KEYS);
	}

	private static AuthProperties properties(int loginLimit) {
		return new AuthProperties(
				Duration.ofMinutes(15),
				Duration.ofHours(8),
				new AuthProperties.Cookie("cineflow_refresh", false, "Lax", "/api/auth"),
				loginLimit,
				30,
				Duration.ofMinutes(1),
				new AuthProperties.Jwt("local-dev-only-jwt-secret-key-32b"));
	}
}
