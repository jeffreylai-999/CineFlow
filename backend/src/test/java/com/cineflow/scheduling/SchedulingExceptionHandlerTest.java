package com.cineflow.scheduling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

class SchedulingExceptionHandlerTest {

	@Test
	void concurrentHoldDuringRemoveIsAConflictNotAServerError() {
		SchedulingExceptionHandler handler = new SchedulingExceptionHandler();
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
				"ERROR: update or delete on table \"showtimes\" violates foreign key constraint \"seat_claims_showtime_hall_fk\"");

		ResponseEntity<ProblemDetail> response = handler.handleDataAccess(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTitle()).isEqualTo("Only unused future Showtimes can be removed");
		assertThat(response.getBody().getProperties()).containsEntry("code", "scheduling.showtime_not_removable");
	}
}
