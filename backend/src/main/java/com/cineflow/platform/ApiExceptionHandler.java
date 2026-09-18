package com.cineflow.platform;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(invalidRequestProblem());
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException exception,
			HttpHeaders headers,
			HttpStatusCode status,
			WebRequest request) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(invalidRequestProblem());
	}

	private static ProblemDetail notFoundProblem() {
		return problem(HttpStatus.NOT_FOUND, "resource.not_found", "Not Found");
	}

	private static ProblemDetail invalidRequestProblem() {
		return problem(HttpStatus.BAD_REQUEST, "request.invalid", "Bad Request");
	}

	private static ProblemDetail problem(HttpStatus status, String code, String title) {
		ProblemDetail problem = ProblemDetail.forStatus(status);
		problem.setTitle(title);
		problem.setProperty("code", code);
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		if (correlationId != null && !correlationId.isBlank()) {
			problem.setProperty("correlationId", correlationId);
		}
		return problem;
	}
}
