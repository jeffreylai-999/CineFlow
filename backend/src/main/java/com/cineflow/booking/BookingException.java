package com.cineflow.booking;

import org.springframework.http.HttpStatus;

public class BookingException extends RuntimeException {

	private final HttpStatus status;
	private final String code;
	private final String title;

	private BookingException(HttpStatus status, String code, String title) {
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

	public static BookingException showtimeNotFound() {
		return new BookingException(HttpStatus.NOT_FOUND, "booking.showtime_not_found", "Showtime not found");
	}

	public static BookingException cutoff() {
		return new BookingException(
				HttpStatus.CONFLICT,
				"booking.cutoff",
				"Online checkout is closed for this Showtime");
	}
}
