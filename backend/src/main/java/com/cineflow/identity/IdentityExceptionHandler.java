package com.cineflow.identity;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cineflow.platform.CorrelationIdFilter;

@RestControllerAdvice
class IdentityExceptionHandler {

	@ExceptionHandler(IdentityException.class)
	ResponseEntity<ProblemDetail> handleIdentity(IdentityException exception) {
		return problem(exception.status(), exception.code(), exception.title());
	}

	@ExceptionHandler(RateLimitException.class)
	ResponseEntity<ProblemDetail> handleRateLimit(RateLimitException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(exception.status());
		problem.setTitle(exception.title());
		problem.setProperty("code", exception.code());
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		if (correlationId != null && !correlationId.isBlank()) {
			problem.setProperty("correlationId", correlationId);
		}
		return ResponseEntity.status(exception.status())
			.header(HttpHeaders.RETRY_AFTER, Long.toString(exception.retryAfter().toSeconds()))
			.body(problem);
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
