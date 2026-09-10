package com.cineflow.platform;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleNoResourceFoundException(
			NoResourceFoundException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundProblem());
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
			org.springframework.web.servlet.NoHandlerFoundException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundProblem());
	}

	private static ProblemDetail notFoundProblem() {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setTitle("Not Found");
		problem.setProperty("code", "resource.not_found");
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		if (correlationId != null && !correlationId.isBlank()) {
			problem.setProperty("correlationId", correlationId);
		}
		return problem;
	}
}
