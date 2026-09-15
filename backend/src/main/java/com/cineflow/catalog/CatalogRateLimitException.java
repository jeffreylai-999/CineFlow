package com.cineflow.catalog;

import java.time.Duration;

import org.springframework.http.HttpStatus;

public class CatalogRateLimitException extends RuntimeException {

	private final Duration retryAfter;

	public CatalogRateLimitException(Duration retryAfter) {
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
		return "catalog.rate_limited";
	}

	public String title() {
		return "Too Many Requests";
	}
}
