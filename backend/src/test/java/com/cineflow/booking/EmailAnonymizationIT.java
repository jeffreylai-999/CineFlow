package com.cineflow.booking;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
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
class EmailAnonymizationIT {

	private static final Instant AS_OF = Instant.parse("2026-09-16T12:00:00Z");
	private static final long SEVEN_DAYS = 7 * 24 * 60 * 60;
	private static final String ANONYMIZED = "anonymized@cineflow.invalid";

	@Autowired
	JdbcTemplate jdbcTemplate;

	@BeforeEach
	void removeBookings() {
		jdbcTemplate.update("delete from cineflow.seat_claims where booking_id is not null");
		jdbcTemplate.update("delete from cineflow.payments");
		jdbcTemplate.update("delete from cineflow.bookings");
	}

	@Test
	void anEmailIsAnonymizedExactlySevenDaysAfterTheShowtime() {
		long showtimeId = insertShowtimeAt(Timestamp.from(AS_OF.minusSeconds(SEVEN_DAYS)));
		insertBooking(showtimeId, "aisyah@example.com");

		int anonymized = anonymizeAsOf(AS_OF);

		assertThat(anonymized).isOne();
		assertThat(emailOf(showtimeId)).isEqualTo(ANONYMIZED);
	}

	@Test
	void anEmailIsRetainedOneSecondBeforeTheRetentionBoundary() {
		long showtimeId = insertShowtimeAt(Timestamp.from(AS_OF.minusSeconds(SEVEN_DAYS - 1)));
		insertBooking(showtimeId, "aisyah@example.com");

		int anonymized = anonymizeAsOf(AS_OF);

		assertThat(anonymized).isZero();
		assertThat(emailOf(showtimeId)).isEqualTo("aisyah@example.com");
	}

	@Test
	void repeatedRunsAreIdempotent() {
		long showtimeId = insertShowtimeAt(Timestamp.from(AS_OF.minusSeconds(SEVEN_DAYS + 60)));
		insertBooking(showtimeId, "aisyah@example.com");

		assertThat(anonymizeAsOf(AS_OF)).isOne();
		assertThat(anonymizeAsOf(AS_OF)).isZero();
		assertThat(emailOf(showtimeId)).isEqualTo(ANONYMIZED);
	}

	@Test
	void onlyBookingsPastRetentionAreAnonymized() {
		long expiredShowtimeId = insertShowtimeAt(Timestamp.from(AS_OF.minusSeconds(SEVEN_DAYS + 3600)));
		long currentShowtimeId = insertShowtimeAt(Timestamp.from(AS_OF.minusSeconds(3600)));
		insertBooking(expiredShowtimeId, "expired@example.com");
		insertBooking(currentShowtimeId, "current@example.com");

		int anonymized = anonymizeAsOf(AS_OF);

		assertThat(anonymized).isOne();
		assertThat(emailOf(expiredShowtimeId)).isEqualTo(ANONYMIZED);
		assertThat(emailOf(currentShowtimeId)).isEqualTo("current@example.com");
	}

	@Test
	void anonymizationPreservesBookingPaymentSeatsAndAdmissionHistory() {
		long showtimeId = insertShowtimeAt(Timestamp.from(AS_OF.minusSeconds(SEVEN_DAYS - 1)));
		long bookingId = insertBooking(showtimeId, "aisyah@example.com");
		jdbcTemplate.update(
				"""
						insert into cineflow.payments (booking_id, amount_myr, method, idempotency_key, request_fingerprint, created_at)
						values (?, 46.00, 'CARD_SIMULATED', ?, 'fingerprint', ?)
						""",
				bookingId,
				UUID.randomUUID().toString(),
				Timestamp.from(AS_OF));
		int hallId = jdbcTemplate.queryForObject(
				"select hall_id from cineflow.showtimes where id = ?",
				Integer.class,
				showtimeId);
		int seatId = insertSeat(hallId);
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (
						    showtime_id, hall_id, seat_id, claim_kind, booking_id, ticket_type, price_myr)
						values (?, ?, ?, 'BOOKING', ?, 'ADULT', 28.00)
						""",
				showtimeId,
				hallId,
				seatId,
				bookingId);
		var before = jdbcTemplate.queryForMap(
				"""
						select email, booking_reference, admission_token_hash, created_at
						from cineflow.bookings where id = ?
						""",
				bookingId);

		assertThat(anonymizeAsOf(AS_OF.plusSeconds(SEVEN_DAYS + 1))).isOne();

		var after = jdbcTemplate.queryForMap(
				"""
						select email, booking_reference, admission_token_hash, created_at
						from cineflow.bookings where id = ?
						""",
				bookingId);
		assertThat(after.get("email")).isEqualTo(ANONYMIZED);
		assertThat(after.get("booking_reference")).isEqualTo(before.get("booking_reference"));
		assertThat(after.get("admission_token_hash")).isEqualTo(before.get("admission_token_hash"));
		assertThat(after.get("created_at")).isEqualTo(before.get("created_at"));

		var payment = jdbcTemplate.queryForMap(
				"select amount_myr, method from cineflow.payments where booking_id = ?",
				bookingId);
		assertThat(payment.get("amount_myr").toString()).isEqualTo("46.00");
		assertThat(payment.get("method")).isEqualTo("CARD_SIMULATED");

		var claim = jdbcTemplate.queryForMap(
				"""
						select claim_kind, booking_id, ticket_type, price_myr
						from cineflow.seat_claims where showtime_id = ? and seat_id = ?
						""",
				showtimeId,
				seatId);
		assertThat(claim.get("claim_kind")).isEqualTo("BOOKING");
		assertThat(((Number) claim.get("booking_id")).longValue()).isEqualTo(bookingId);
		assertThat(claim.get("ticket_type")).isEqualTo("ADULT");
		assertThat(claim.get("price_myr").toString()).isEqualTo("28.00");
	}

	@Test
	void theScheduledDefaultRunUsesTheDatabaseClock() {
		long showtimeId = insertShowtimeAt(Timestamp.from(Instant.now().minusSeconds(SEVEN_DAYS + 3600)));
		insertBooking(showtimeId, "aisyah@example.com");

		Integer anonymized = jdbcTemplate.queryForObject(
				"select cineflow.anonymize_expired_booking_emails()",
				Integer.class);

		assertThat(anonymized).isNotNull();
		assertThat(emailOf(showtimeId)).isEqualTo(ANONYMIZED);
	}

	@Test
	void theAnonymizationJobIsScheduledWherePgCronIsAvailable() {
		Boolean pgCronAvailable = jdbcTemplate.queryForObject(
				"select exists (select 1 from pg_available_extensions where name = 'pg_cron')",
				Boolean.class);
		Assumptions.assumeTrue(
				Boolean.TRUE.equals(pgCronAvailable),
				"pg_cron is only available on Supabase; the schedule is wired by V9 there");

		String schedule = jdbcTemplate.queryForObject(
				"select schedule from cron.job where jobname = 'anonymize-expired-booking-emails'",
				String.class);

		assertThat(schedule).isEqualTo("17 * * * *");
	}

	private int anonymizeAsOf(Instant asOf) {
		Integer anonymized = jdbcTemplate.queryForObject(
				"select cineflow.anonymize_expired_booking_emails(?)",
				Integer.class,
				Timestamp.from(asOf));
		return anonymized == null ? 0 : anonymized;
	}

	private String emailOf(long showtimeId) {
		return jdbcTemplate.queryForObject(
				"select email from cineflow.bookings where showtime_id = ?",
				String.class,
				showtimeId);
	}

	private long insertShowtimeAt(Timestamp startsAt) {
		long movieId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.movies (
						    title, synopsis, genre, runtime_minutes, age_rating, poster_url,
						    source_provider, external_id, source_refreshed_at)
						values (?, 'Synopsis', 'Adventure', 90, 'PG', null, 'fixture', ?, ?)
						returning id
						""",
				Long.class,
				"Anonymization " + UUID.randomUUID(),
				UUID.randomUUID().toString(),
				startsAt);
		int hallId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.halls (name, row_count, seats_per_row, seat_map_locked, created_at)
						values (?, 1, 1, false, ?)
						returning id
						""",
				Integer.class,
				"Hall " + UUID.randomUUID(),
				startsAt);
		return jdbcTemplate.queryForObject(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
						values (?, ?, ?, 28.00, 18.00)
						returning id
						""",
				Long.class,
				hallId,
				movieId,
				startsAt);
	}

	private long insertBooking(long showtimeId, String email) {
		String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
		Long bookingId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.bookings (showtime_id, email, booking_reference, admission_token_hash, created_at)
						values (?, ?, ?, ?, ?)
						returning id
						""",
				Long.class,
				showtimeId,
				email,
				"REF" + suffix,
				"hash-" + suffix,
				Timestamp.from(AS_OF.minusSeconds(30 * 24 * 60 * 60L)));
		return bookingId;
	}

	private int insertSeat(int hallId) {
		return jdbcTemplate.queryForObject(
				"""
						insert into cineflow.seats (hall_id, row_label, seat_number, disabled)
						values (?, 'A', 1, false)
						returning id
						""",
				Integer.class,
				hallId);
	}
}
