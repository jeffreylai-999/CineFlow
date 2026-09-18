package com.cineflow.admission;

import org.springframework.http.HttpStatus;

public class AdmissionException extends RuntimeException {

	private final HttpStatus status;
	private final String code;
	private final String title;

	private AdmissionException(HttpStatus status, String code, String title) {
		super(title);
		this.status = status;
		this.code = code;
		this.title = title;
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

	public static AdmissionException invalidRequest() {
		return new AdmissionException(HttpStatus.BAD_REQUEST, "request.invalid", "Bad Request");
	}

	public static AdmissionException notFound() {
		return new AdmissionException(HttpStatus.NOT_FOUND, "admission.not_found", "Booking not found");
	}

	public static AdmissionException alreadyAdmitted() {
		return new AdmissionException(
				HttpStatus.CONFLICT,
				"admission.already_admitted",
				"This Booking was already admitted");
	}
}
