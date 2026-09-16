package com.cineflow.catalog;

import java.time.Duration;

import org.springframework.http.HttpStatus;

public class CatalogException extends RuntimeException {

	private static final Duration PROVIDER_QUOTA_RETRY_AFTER = Duration.ofSeconds(60);

	private final HttpStatus status;
	private final String code;
	private final String title;
	private final Duration retryAfter;

	private CatalogException(HttpStatus status, String code, String title) {
		this(status, code, title, null);
	}

	private CatalogException(HttpStatus status, String code, String title, Duration retryAfter) {
		super(title);
		this.status = status;
		this.code = code;
		this.title = title;
		this.retryAfter = retryAfter;
	}

	public HttpStatus status() {
		return status;
	}

	public String code() {
		return code;
	}

	public String title() {
		return title;
	}

	public Duration retryAfter() {
		return retryAfter;
	}

	public static CatalogException invalidRequest() {
		return new CatalogException(HttpStatus.BAD_REQUEST, "catalog.invalid_request", "Invalid request");
	}

	public static CatalogException providerMismatch() {
		return new CatalogException(
				HttpStatus.CONFLICT,
				"catalog.provider_mismatch",
				"Import no longer matches the active provider");
	}

	public static CatalogException duplicateImport() {
		return new CatalogException(HttpStatus.CONFLICT, "catalog.duplicate_import", "Movie already imported");
	}

	public static CatalogException movieNotFound() {
		return new CatalogException(HttpStatus.NOT_FOUND, "catalog.movie_not_found", "Movie not found");
	}

	public static CatalogException schedulingFieldsRequired() {
		return new CatalogException(
				HttpStatus.BAD_REQUEST,
				"catalog.scheduling_fields_required",
				"Runtime and age rating are required");
	}

	public static CatalogException providerNotConfigured() {
		return new CatalogException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"catalog.provider_not_configured",
				"Movie metadata provider is not configured");
	}

	public static CatalogException providerUnavailable() {
		return new CatalogException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"catalog.provider_unavailable",
				"Movie metadata provider is unavailable");
	}

	public static CatalogException providerQuota() {
		return new CatalogException(
				HttpStatus.TOO_MANY_REQUESTS,
				"catalog.provider_quota",
				"Movie metadata provider is temporarily limited",
				PROVIDER_QUOTA_RETRY_AFTER);
	}

	public static CatalogException saveFailed() {
		return new CatalogException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"catalog.save_failed",
				"Unable to save the Movie");
	}

	static CatalogException from(MovieProviderException exception) {
		return switch (exception.kind()) {
			case NOT_CONFIGURED -> providerNotConfigured();
			case UNAVAILABLE -> providerUnavailable();
			case QUOTA -> providerQuota();
			case NOT_FOUND -> movieNotFound();
		};
	}
}
