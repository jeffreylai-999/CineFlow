package com.cineflow.scheduling;

import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cineflow.platform.CorrelationIdFilter;

@RestControllerAdvice(assignableTypes = HallController.class)
class SchedulingExceptionHandler {

	@ExceptionHandler(SchedulingException.class)
	ResponseEntity<ProblemDetail> handleScheduling(SchedulingException exception) {
		return problem(exception.status(), exception.code(), exception.title());
	}

	@ExceptionHandler(DataAccessException.class)
	ResponseEntity<ProblemDetail> handleDataAccess(DataAccessException exception) {
		String message = String.valueOf(exception.getMostSpecificCause().getMessage());
		if (message.contains("scheduling.seat_not_disableable")) {
			return problem(
					HttpStatus.CONFLICT, "scheduling.seat_not_disableable", "Seat cannot be disabled");
		}
		throw exception;
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
