package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cineflow.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MovieLifecycleIT {

	private static final Instant AS_OF = Instant.parse("2026-09-16T12:00:00Z");

	@Autowired
	JdbcTemplate jdbcTemplate;

	@BeforeEach
	void clearLifecycleRows() {
		jdbcTemplate.update("delete from cineflow.seat_claims");
		jdbcTemplate.update("delete from cineflow.seat_holds");
		jdbcTemplate.update("delete from cineflow.payments");
		jdbcTemplate.update("delete from cineflow.bookings");
		jdbcTemplate.update(
				"""
						delete from cineflow.showtimes
						where movie_id in (select id from cineflow.movies where source_provider = 'lifecycle')
						""");
		jdbcTemplate.update("delete from cineflow.movies where source_provider = 'lifecycle'");
		jdbcTemplate.update("delete from cineflow.audit_events where action = 'MOVIE_ARCHIVED'");
		jdbcTemplate.update("delete from cineflow.refresh_tokens");
		jdbcTemplate.update("delete from cineflow.refresh_token_families");
	}

	@Test
	void archivesAMovieOnlyAfterAtLeastOneEndedShowtimeAndNoCurrentOrFutureShowtime() {
		long movieId = insertMovie(90);
		insertShowtime(movieId, AS_OF.minusSeconds(3 * 60 * 60));

		assertThat(archivedAt(movieId)).isNull();
		archiveAsOf(AS_OF);
		assertThat(archivedAt(movieId)).isEqualTo(Timestamp.from(AS_OF));
	}

	@Test
	void doesNotArchiveWhenACurrentOrFutureShowtimeRemains() {
		long movieId = insertMovie(90);
		insertShowtime(movieId, AS_OF.minusSeconds(30 * 60));

		archiveAsOf(AS_OF);
		assertThat(archivedAt(movieId)).isNull();
	}

	@Test
	void doesNotArchiveMoviesThatNeverHadAShowtime() {
		long movieId = insertMovie(90);

		archiveAsOf(AS_OF);
		assertThat(archivedAt(movieId)).isNull();
	}

	@Test
	void archivalIsIdempotentAndWritesASystemAuditEvent() {
		long movieId = insertMovie(90);
		insertShowtime(movieId, AS_OF.minusSeconds(4 * 60 * 60));

		archiveAsOf(AS_OF);
		assertThat(archivedAt(movieId)).isEqualTo(Timestamp.from(AS_OF));
		archiveAsOf(AS_OF);
		assertThat(archivedAt(movieId)).isEqualTo(Timestamp.from(AS_OF));

		List<Map<String, Object>> events = jdbcTemplate.queryForList(
				"""
						select actor_staff_id, action, subject_type, subject_id
						from cineflow.audit_events
						where action = 'MOVIE_ARCHIVED' and subject_id = ?
						""",
				Long.toString(movieId));
		assertThat(events).hasSize(1);
		assertThat(events.getFirst().get("actor_staff_id")).isNull();
		assertThat(events.getFirst().get("subject_type")).isEqualTo("movie");
	}

	@Test
	void expiredSeatHoldCleanupRemovesHoldsWithoutTouchingBookings() {
		long movieId = insertMovie(90);
		int hallId = insertHall(false);
		int holdSeatId = insertSeat(hallId, "A", 1);
		int bookedSeatId = insertSeat(hallId, "A", 2);
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		long showtimeId = insertShowtimeInHall(movieId, hallId, AS_OF.plusSeconds(2 * 60 * 60));
		UUID holdId = UUID.randomUUID();
		jdbcTemplate.update(
				"insert into cineflow.seat_holds (id, showtime_id, expires_at) values (?, ?, ?)",
				holdId,
				showtimeId,
				Timestamp.from(AS_OF.minusSeconds(60)));
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (
						    showtime_id, hall_id, seat_id, claim_kind, expires_at, hold_id)
						values (?, ?, ?, 'HOLD', ?, ?)
						""",
				showtimeId,
				hallId,
				holdSeatId,
				Timestamp.from(AS_OF.minusSeconds(60)),
				holdId);
		long bookingId = insertBooking(showtimeId);
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (
						    showtime_id, hall_id, seat_id, claim_kind, booking_id, ticket_type, price_myr)
						values (?, ?, ?, 'BOOKING', ?, 'ADULT', 28.00)
						""",
				showtimeId,
				hallId,
				bookedSeatId,
				bookingId);

		assertThat(cleanupHoldsAsOf(AS_OF)).isGreaterThanOrEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
						"select count(*) from cineflow.seat_holds where id = ?",
						Integer.class,
						holdId))
				.isZero();
		assertThat(jdbcTemplate.queryForObject(
						"select count(*) from cineflow.seat_claims where seat_id = ?",
						Integer.class,
						holdSeatId))
				.isZero();
		assertThat(jdbcTemplate.queryForObject(
						"select claim_kind from cineflow.seat_claims where seat_id = ?",
						String.class,
						bookedSeatId))
				.isEqualTo("BOOKING");
	}

	@Test
	void expiredRefreshTokenCleanupKeepsReuseTombstonesWhileAFamilyTokenIsLive() {
		long staffId = jdbcTemplate.queryForObject(
				"select id from cineflow.staff_accounts where username = 'administrator'",
				Long.class);
		long familyId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.refresh_token_families (staff_account_id, created_at)
						values (?, ?)
						returning id
						""",
				Long.class,
				staffId,
				Timestamp.from(AS_OF.minusSeconds(9 * 60 * 60)));
		long expiredId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.refresh_tokens (
						    family_id, token_hash, expires_at, created_at)
						values (?, ?, ?, ?)
						returning id
						""",
				Long.class,
				familyId,
				"expired-" + UUID.randomUUID(),
				Timestamp.from(AS_OF.minusSeconds(60)),
				Timestamp.from(AS_OF.minusSeconds(8 * 60 * 60)));
		long currentId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.refresh_tokens (
						    family_id, token_hash, expires_at, created_at)
						values (?, ?, ?, ?)
						returning id
						""",
				Long.class,
				familyId,
				"current-" + UUID.randomUUID(),
				Timestamp.from(AS_OF.plusSeconds(60 * 60)),
				Timestamp.from(AS_OF.minusSeconds(30)));
		jdbcTemplate.update(
				"update cineflow.refresh_tokens set replaced_by_id = ?, revoked_at = ? where id = ?",
				currentId,
				Timestamp.from(AS_OF.minusSeconds(30)),
				expiredId);

		assertThat(cleanupTokensAsOf(AS_OF)).isZero();
		assertThat(jdbcTemplate.queryForObject(
						"select count(*) from cineflow.refresh_tokens where id = ?",
						Integer.class,
						expiredId))
				.isOne();
		assertThat(jdbcTemplate.queryForObject(
						"select count(*) from cineflow.refresh_tokens where id = ?",
						Integer.class,
						currentId))
				.isOne();

		jdbcTemplate.update(
				"update cineflow.refresh_tokens set expires_at = ? where id = ?",
				Timestamp.from(AS_OF.minusSeconds(1)),
				currentId);
		assertThat(cleanupTokensAsOf(AS_OF)).isEqualTo(2);
		assertThat(cleanupTokensAsOf(AS_OF)).isZero();
		assertThat(jdbcTemplate.queryForObject(
						"select count(*) from cineflow.refresh_tokens where family_id = ?",
						Integer.class,
						familyId))
				.isZero();
		assertThat(jdbcTemplate.queryForObject(
						"select count(*) from cineflow.refresh_token_families where id = ?",
						Integer.class,
						familyId))
				.isZero();
	}

	@Test
	void cronJobHistoryCleanupIsSafeWithoutPgCron() {
		Integer removed = jdbcTemplate.queryForObject(
				"select cineflow.cleanup_cron_job_history(?)",
				Integer.class,
				Timestamp.from(AS_OF));
		assertThat(removed).isNotNull();
		assertThat(removed).isGreaterThanOrEqualTo(0);
	}

	@Test
	void cronJobsAreScheduledWherePgCronIsAvailable() {
		Boolean pgCronAvailable = jdbcTemplate.queryForObject(
				"select exists (select 1 from pg_available_extensions where name = 'pg_cron')",
				Boolean.class);
		Assumptions.assumeTrue(
				Boolean.TRUE.equals(pgCronAvailable),
				"pg_cron is only available on Supabase; schedules are wired by V12 there");

		assertThat(jobSchedule("archive-eligible-movies")).isEqualTo("23 * * * *");
		assertThat(jobSchedule("cleanup-expired-seat-holds")).isEqualTo("29 * * * *");
		assertThat(jobSchedule("cleanup-expired-refresh-tokens")).isEqualTo("37 * * * *");
		assertThat(jobSchedule("cleanup-cron-job-history")).isEqualTo("47 3 * * *");
	}

	private int archiveAsOf(Instant asOf) {
		Integer count = jdbcTemplate.queryForObject(
				"select cineflow.archive_eligible_movies(?)",
				Integer.class,
				Timestamp.from(asOf));
		return count == null ? 0 : count;
	}

	private int cleanupHoldsAsOf(Instant asOf) {
		Integer count = jdbcTemplate.queryForObject(
				"select cineflow.cleanup_expired_seat_holds(?)",
				Integer.class,
				Timestamp.from(asOf));
		return count == null ? 0 : count;
	}

	private int cleanupTokensAsOf(Instant asOf) {
		Integer count = jdbcTemplate.queryForObject(
				"select cineflow.cleanup_expired_refresh_tokens(?)",
				Integer.class,
				Timestamp.from(asOf));
		return count == null ? 0 : count;
	}

	private String jobSchedule(String jobName) {
		return jdbcTemplate.queryForObject(
				"select schedule from cron.job where jobname = ?",
				String.class,
				jobName);
	}

	private Timestamp archivedAt(long movieId) {
		return jdbcTemplate.queryForObject(
				"select archived_at from cineflow.movies where id = ?",
				Timestamp.class,
				movieId);
	}

	private long insertMovie(int runtimeMinutes) {
		return jdbcTemplate.queryForObject(
				"""
						insert into cineflow.movies (
						    title, synopsis, genre, runtime_minutes, age_rating, poster_url,
						    source_provider, external_id, source_refreshed_at)
						values (?, 'Synopsis', 'Adventure', ?, 'PG', null, 'lifecycle', ?, ?)
						returning id
						""",
				Long.class,
				"Lifecycle " + UUID.randomUUID(),
				runtimeMinutes,
				UUID.randomUUID().toString(),
				Timestamp.from(AS_OF.minusSeconds(30 * 24 * 60 * 60L)));
	}

	private long insertShowtime(long movieId, Instant startsAt) {
		int hallId = insertHall(false);
		return insertShowtimeInHall(movieId, hallId, startsAt);
	}

	private int insertHall(boolean locked) {
		return jdbcTemplate.queryForObject(
				"""
						insert into cineflow.halls (name, row_count, seats_per_row, seat_map_locked, created_at)
						values (?, 1, 2, ?, ?)
						returning id
						""",
				Integer.class,
				"Hall " + UUID.randomUUID(),
				locked,
				Timestamp.from(AS_OF));
	}

	private long insertShowtimeInHall(long movieId, int hallId, Instant startsAt) {
		return jdbcTemplate.queryForObject(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
						values (?, ?, ?, 28.00, 18.00)
						returning id
						""",
				Long.class,
				hallId,
				movieId,
				Timestamp.from(startsAt));
	}

	private int insertSeat(int hallId, String row, int number) {
		return jdbcTemplate.queryForObject(
				"""
						insert into cineflow.seats (hall_id, row_label, seat_number, disabled)
						values (?, ?, ?, false)
						returning id
						""",
				Integer.class,
				hallId,
				row,
				number);
	}

	private long insertBooking(long showtimeId) {
		String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
		return jdbcTemplate.queryForObject(
				"""
						insert into cineflow.bookings (showtime_id, email, booking_reference, admission_token_hash, created_at)
						values (?, ?, ?, ?, ?)
						returning id
						""",
				Long.class,
				showtimeId,
				"guest@example.com",
				"REF" + suffix,
				"hash-" + suffix,
				Timestamp.from(AS_OF));
	}
}
