package com.cineflow.identity;

import org.springframework.http.HttpStatus;

public class IdentityException extends RuntimeException {

	private final HttpStatus status;
	private final String code;
	private final String title;

	private IdentityException(HttpStatus status, String code, String title) {
		super(title);
		this.status = status;
		this.code = code;
		this.title = title;
	}

	public static IdentityException invalidCredentials() {
		return new IdentityException(HttpStatus.UNAUTHORIZED, "auth.invalid_credentials", "Unauthorized");
	}

	public static IdentityException unauthorized() {
		return new IdentityException(HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Unauthorized");
	}

	public static IdentityException tokenReused() {
		return new IdentityException(HttpStatus.UNAUTHORIZED, "auth.token_reused", "Unauthorized");
	}

	public static IdentityException forbidden() {
		return new IdentityException(HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden");
	}

	public static IdentityException usernameConflict() {
		return new IdentityException(HttpStatus.CONFLICT, "staff.username_conflict", "Username already exists");
	}

	public static IdentityException invalidRequest() {
		return new IdentityException(HttpStatus.BAD_REQUEST, "request.invalid", "Bad Request");
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
}
