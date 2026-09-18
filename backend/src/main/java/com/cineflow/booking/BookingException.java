package com.cineflow.booking;

import java.time.Duration;

import org.springframework.http.HttpStatus;

public class BookingException extends RuntimeException {

	private final HttpStatus status;
	private final String code;
	private final String title;
	private final Duration retryAfter;

	private BookingException(HttpStatus status, String code, String title) {
		this(status, code, title, null);
	}

	private BookingException(HttpStatus status, String code, String title, Duration retryAfter) {
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

	public static BookingException showtimeNotFound() {
		return new BookingException(HttpStatus.NOT_FOUND, "booking.showtime_not_found", "Showtime not found");
	}

	public static BookingException cutoff() {
		return new BookingException(
				HttpStatus.CONFLICT,
				"booking.cutoff",
				"Online checkout is closed at the Booking Cutoff");
	}

	public static BookingException seatsUnavailable() {
		return new BookingException(
				HttpStatus.CONFLICT,
				"booking.seats_unavailable",
				"One or more Seats are unavailable");
	}

	public static BookingException invalidSeatSelection() {
		return new BookingException(
				HttpStatus.BAD_REQUEST,
				"booking.invalid_seat_selection",
				"Seat selection is invalid");
	}

	public static BookingException bookingLimit() {
		return new BookingException(
				HttpStatus.CONFLICT,
				"booking.limit",
				"The Seat selection exceeds the Booking Limit");
	}

	public static BookingException holdNotFound() {
		return new BookingException(HttpStatus.NOT_FOUND, "booking.hold_not_found", "Seat Hold not found");
	}

	public static BookingException holdExpired() {
		return new BookingException(HttpStatus.CONFLICT, "booking.hold_expired", "The Seat Hold has expired");
	}

	public static BookingException holdUnavailable() {
		return new BookingException(
				HttpStatus.CONFLICT,
				"booking.hold_unavailable",
				"The Seat Hold is no longer available");
	}

	public static BookingException paymentDeclined() {
		return new BookingException(
				HttpStatus.PAYMENT_REQUIRED,
				"booking.payment_declined",
				"Payment was declined");
	}

	public static BookingException idempotencyConflict() {
		return new BookingException(
				HttpStatus.CONFLICT,
				"booking.idempotency_conflict",
				"The Idempotency Key was already used for a different request");
	}

	public static BookingException retrievalFailed() {
		return new BookingException(
				HttpStatus.NOT_FOUND,
				"booking.retrieval_failed",
				"Booking not found");
	}

	public static BookingException rateLimited(Duration retryAfter) {
		return new BookingException(
				HttpStatus.TOO_MANY_REQUESTS,
				"booking.rate_limited",
				"Too Many Requests",
				retryAfter);
	}
}
