package com.cineflow.booking;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SeatHoldExpiryService {

	private final JdbcTemplate jdbcTemplate;
	private final Clock clock;
	private final SeatAvailabilityPublisher availabilityPublisher;

	SeatHoldExpiryService(
			JdbcTemplate jdbcTemplate,
			Clock clock,
			SeatAvailabilityPublisher availabilityPublisher) {
		this.jdbcTemplate = jdbcTemplate;
		this.clock = clock;
		this.availabilityPublisher = availabilityPublisher;
	}

	@Scheduled(fixedDelay = 1_000)
	@Transactional
	void releaseExpiredHolds() {
		Instant now = clock.instant();
		List<Long> affectedShowtimes = jdbcTemplate.query(
				"""
						delete from cineflow.seat_claims
						where claim_kind = 'HOLD' and expires_at <= ?
						returning showtime_id
						""",
				(resultSet, rowNum) -> resultSet.getLong("showtime_id"),
				Timestamp.from(now))
			.stream()
			.distinct()
			.toList();
		jdbcTemplate.update(
				"""
						delete from cineflow.seat_holds hold
						where hold.expires_at <= ?
						  and not exists (
						      select 1 from cineflow.seat_claims claim where claim.hold_id = hold.id)
						""",
				Timestamp.from(now));
		for (Long showtimeId : affectedShowtimes) {
			availabilityPublisher.publishAfterCommit(showtimeId);
		}
	}
}
