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

	public static SchedulingException invalidCinemaTime() {
		return new SchedulingException(
				HttpStatus.BAD_REQUEST,
				"scheduling.invalid_cinema_time",
				"Showtime must use Cinema Time (Asia/Kuala_Lumpur)");
	}

	public static SchedulingException movieNotFound() {
		return new SchedulingException(HttpStatus.NOT_FOUND, "scheduling.movie_not_found", "Movie not found");
	}

	public static SchedulingException movieArchived() {
		return new SchedulingException(
				HttpStatus.CONFLICT, "scheduling.movie_archived", "Archived Movies cannot receive new Showtimes");
	}

	public static SchedulingException hallArchived() {
		return new SchedulingException(
				HttpStatus.CONFLICT, "scheduling.hall_archived", "Archived Halls cannot receive new Showtimes");
	}

	public static SchedulingException showtimeNotFound() {
		return new SchedulingException(HttpStatus.NOT_FOUND, "scheduling.showtime_not_found", "Showtime not found");
	}

	public static SchedulingException showtimeOverlap() {
		return new SchedulingException(
				HttpStatus.CONFLICT,
				"scheduling.showtime_overlap",
				"That Hall is occupied through the movie runtime and Cleaning Buffer");
	}

	public static SchedulingException showtimeHasBookings() {
		return new SchedulingException(
				HttpStatus.CONFLICT, "scheduling.showtime_has_bookings", "Showtimes with Bookings cannot be removed");
	}

	public static SchedulingException showtimeNotRemovable() {
		return new SchedulingException(
				HttpStatus.CONFLICT, "scheduling.showtime_not_removable", "Only unused future Showtimes can be removed");
	}

	public static SchedulingException invalidPrice() {
		return new SchedulingException(
				HttpStatus.BAD_REQUEST, "scheduling.invalid_price", "Adult and Child prices must be greater than zero");
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
