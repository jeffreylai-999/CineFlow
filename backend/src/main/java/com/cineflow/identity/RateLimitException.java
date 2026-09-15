package com.cineflow.identity;

import java.time.Duration;

import org.springframework.http.HttpStatus;

public class RateLimitException extends RuntimeException {

	private final Duration retryAfter;

	public RateLimitException(Duration retryAfter) {
		super("Too Many Requests");
		this.retryAfter = retryAfter;
	}

	public Duration retryAfter() {
		return retryAfter;
	}

	public HttpStatus status() {
		return HttpStatus.TOO_MANY_REQUESTS;
	}

	public String code() {
		return "auth.rate_limited";
	}

	public String title() {
		return "Too Many Requests";
	}
}
