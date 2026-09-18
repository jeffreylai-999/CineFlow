package com.cineflow.admission;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cineflow.platform.CorrelationIdFilter;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.cineflow.admission")
class AdmissionExceptionHandler {

	@ExceptionHandler(AdmissionException.class)
	ResponseEntity<ProblemDetail> handleAdmission(AdmissionException exception) {
		return ResponseEntity.status(exception.status())
			.body(problem(exception.status(), exception.code(), exception.title()));
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
