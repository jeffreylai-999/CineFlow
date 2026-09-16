package com.cineflow.scheduling;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.JsonMappingException;

import com.cineflow.platform.CorrelationIdFilter;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.cineflow.scheduling")
class SchedulingExceptionHandler {

	@ExceptionHandler(SchedulingException.class)
	ResponseEntity<ProblemDetail> handleScheduling(SchedulingException exception) {
		return problem(exception.status(), exception.code(), exception.title());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> handleInvalid(MethodArgumentNotValidException exception) {
		if (isTicketPriceViolation(exception)) {
			return problem(
					HttpStatus.BAD_REQUEST,
					"scheduling.invalid_price",
					"Adult and Child prices must be greater than zero");
		}
		return problem(HttpStatus.BAD_REQUEST, "request.invalid", "Bad Request");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException exception) {
		if (isMalformedTicketPrice(exception)) {
			return problem(
					HttpStatus.BAD_REQUEST,
					"scheduling.invalid_price",
					"Adult and Child prices must be greater than zero");
		}
		return problem(HttpStatus.BAD_REQUEST, "request.invalid", "Bad Request");
	}

	@ExceptionHandler(DataAccessException.class)
	ResponseEntity<ProblemDetail> handleDataAccess(DataAccessException exception) {
		String message = String.valueOf(exception.getMostSpecificCause().getMessage());
		if (message.contains("scheduling.seat_not_disableable")) {
			return problem(
					HttpStatus.CONFLICT, "scheduling.seat_not_disableable", "Seat cannot be disabled");
		}
		if (message.contains("showtimes_hall_occupancy_excl")) {
			return problem(
					HttpStatus.CONFLICT,
					"scheduling.showtime_overlap",
					"That Hall is occupied through the movie runtime and Cleaning Buffer");
		}
		if (message.contains("scheduling.hall_archived")) {
			return problem(
					HttpStatus.CONFLICT, "scheduling.hall_archived", "Archived Halls cannot receive new Showtimes");
		}
		if (message.contains("scheduling.movie_archived")) {
			return problem(
					HttpStatus.CONFLICT,
					"scheduling.movie_archived",
					"Archived Movies cannot receive new Showtimes");
		}
		if (message.contains("scheduling.showtime_has_bookings")) {
			return problem(
					HttpStatus.CONFLICT,
					"scheduling.showtime_has_bookings",
					"Showtimes with Bookings cannot be removed");
		}
		if (message.contains("seat_claims_showtime_hall_fk")) {
			return problem(
					HttpStatus.CONFLICT,
					"scheduling.showtime_not_removable",
					"Only unused future Showtimes can be removed");
		}
		throw exception;
	}

	private static boolean isTicketPriceViolation(MethodArgumentNotValidException exception) {
		return exception.getBindingResult().getFieldErrors().stream()
			.anyMatch(error -> "adultPriceMyr".equals(error.getField()) || "childPriceMyr".equals(error.getField()));
	}

	private static boolean isMalformedTicketPrice(HttpMessageNotReadableException exception) {
		Throwable current = exception;
		while (current != null) {
			if (current instanceof JsonMappingException mapping
					&& mapping.getPath().stream()
						.map(JsonMappingException.Reference::getFieldName)
						.anyMatch(name -> "adultPriceMyr".equals(name) || "childPriceMyr".equals(name))) {
				return true;
			}
			String message = current.getMessage();
			if (message != null && (message.contains("adultPriceMyr") || message.contains("childPriceMyr"))) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String title) {
		ProblemDetail problem = ProblemDetail.forStatus(status);
		problem.setTitle(title);
		problem.setProperty("code", code);
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		if (correlationId != null && !correlationId.isBlank()) {
			problem.setProperty("correlationId", correlationId);
		}
		return ResponseEntity.status(status).body(problem);
	}
}
