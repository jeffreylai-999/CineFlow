package com.cineflow.scheduling;

import org.springframework.http.HttpStatus;

public class SchedulingException extends RuntimeException {

	private final HttpStatus status;
	private final String code;
	private final String title;

	private SchedulingException(HttpStatus status, String code, String title) {
		super(title);
		this.status = status;
		this.code = code;
		this.title = title;
	}

	public static SchedulingException hallNotFound() {
		return new SchedulingException(HttpStatus.NOT_FOUND, "scheduling.hall_not_found", "Hall not found");
	}

	public static SchedulingException seatNotFound() {
		return new SchedulingException(HttpStatus.NOT_FOUND, "scheduling.seat_not_found", "Seat not found");
	}

	public static SchedulingException seatNotDisableable() {
		return new SchedulingException(
				HttpStatus.CONFLICT, "scheduling.seat_not_disableable", "Seat cannot be disabled");
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
