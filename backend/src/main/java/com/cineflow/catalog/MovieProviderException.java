package com.cineflow.catalog;

public class MovieProviderException extends RuntimeException {

	public enum Kind {
		NOT_CONFIGURED,
		UNAVAILABLE,
		QUOTA,
		NOT_FOUND
	}

	private final Kind kind;

	private MovieProviderException(Kind kind, String message) {
		super(message);
		this.kind = kind;
	}

	public Kind kind() {
		return kind;
	}

	public static MovieProviderException notConfigured() {
		return new MovieProviderException(Kind.NOT_CONFIGURED, "Movie metadata provider is not configured");
	}

	public static MovieProviderException unavailable() {
		return new MovieProviderException(Kind.UNAVAILABLE, "Movie metadata provider is unavailable");
	}

	public static MovieProviderException quota() {
		return new MovieProviderException(Kind.QUOTA, "Movie metadata provider quota was exceeded");
	}

	public static MovieProviderException notFound() {
		return new MovieProviderException(Kind.NOT_FOUND, "Movie was not found at the provider");
	}
}
