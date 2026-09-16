package com.cineflow.catalog;

import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cineflow.platform.CorrelationIdFilter;

@RestControllerAdvice(basePackages = "com.cineflow.catalog")
class CatalogExceptionHandler {

	@ExceptionHandler(CatalogException.class)
	ResponseEntity<ProblemDetail> handleCatalog(CatalogException exception) {
		return problem(exception);
	}

	@ExceptionHandler(MovieProviderException.class)
	ResponseEntity<ProblemDetail> handleProvider(MovieProviderException exception) {
		return problem(CatalogException.from(exception));
	}

	@ExceptionHandler(DataAccessException.class)
	ResponseEntity<ProblemDetail> handleDataAccess(DataAccessException exception) {
		String message = String.valueOf(exception.getMostSpecificCause().getMessage());
		if (message.contains("showtimes_hall_occupancy_excl")) {
			return problem(CatalogException.showtimeOverlap());
		}
		throw exception;
	}

	@ExceptionHandler(CatalogRateLimitException.class)
	ResponseEntity<ProblemDetail> handleRateLimit(CatalogRateLimitException exception) {
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

	private static ResponseEntity<ProblemDetail> problem(CatalogException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(exception.status());
		problem.setTitle(exception.title());
		problem.setProperty("code", exception.code());
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		if (correlationId != null && !correlationId.isBlank()) {
			problem.setProperty("correlationId", correlationId);
		}
		ResponseEntity.BodyBuilder response = ResponseEntity.status(exception.status());
		if (exception.retryAfter() != null) {
			response.header(HttpHeaders.RETRY_AFTER, Long.toString(exception.retryAfter().toSeconds()));
		}
		return response.body(problem);
	}
}
