package com.cineflow.admission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.cineflow.MutableClock;
import com.cineflow.TestcontainersConfiguration;
import com.cineflow.platform.Sha256;
import com.jayway.jsonpath.JsonPath;

@Import({ TestcontainersConfiguration.class, AdmissionIT.ClockConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
class AdmissionIT {

	private static final Instant START = Instant.parse("2026-09-16T00:00:00Z");

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MutableClock clock;

	@Autowired
	Admission admission;

	@Test
	void bookingStaffAdmitsAWholeBookingByScanningTheTicketQrToken() throws Exception {
		clock.set(START);
		Fixture fixture = createBooking("QR Scan");
		String staffToken = accessToken("booking.staff", "StaffPassw0rd!");

		mockMvc.perform(post("/api/staff/admissions")
				.header("Authorization", "Bearer " + staffToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"admissionToken\":\"%s\"}".formatted(fixture.admissionToken())))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.bookingReference").value(fixture.bookingReference()))
			.andExpect(jsonPath("$.movieTitle").value("QR Scan"))
			.andExpect(jsonPath("$.hallName").value(fixture.hallName()))
			.andExpect(jsonPath("$.startsAtCinemaTime").value("2099-06-20T19:30:00"))
			.andExpect(jsonPath("$.timeZone").value("Asia/Kuala_Lumpur"))
			.andExpect(jsonPath("$.seats.length()").value(2))
			.andExpect(jsonPath("$.seats[0].label").value("A1"))
			.andExpect(jsonPath("$.seats[0].ticketType").value("ADULT"))
			.andExpect(jsonPath("$.seats[1].label").value("A2"))
			.andExpect(jsonPath("$.seats[1].ticketType").value("CHILD"))
			.andExpect(jsonPath("$.admittedAt").value("2026-09-16T00:00:00Z"))
			.andExpect(jsonPath("$.email").doesNotExist())
			.andExpect(jsonPath("$.admissionToken").doesNotExist());

		var recorded = jdbcTemplate.queryForMap(
				"select booking_id, admitted_by, admitted_at from cineflow.admissions where booking_id = ?",
				fixture.bookingId());
		assertThat(((Number) recorded.get("booking_id")).longValue()).isEqualTo(fixture.bookingId());
		assertThat(((Number) recorded.get("admitted_by")).longValue()).isEqualTo(staffId("booking.staff"));
		assertThat((Timestamp) recorded.get("admitted_at")).isEqualTo(Timestamp.from(START));

		var audit = jdbcTemplate.queryForMap(
				"""
						select actor_staff_id, subject_type, subject_id, correlation_id
						from cineflow.audit_events
						where action = 'BOOKING_ADMITTED' and subject_id = ?
						""",
				fixture.bookingReference());
		assertThat(((Number) audit.get("actor_staff_id")).longValue()).isEqualTo(staffId("booking.staff"));
		assertThat(audit.get("subject_type")).isEqualTo("booking");
		assertThat(audit.get("correlation_id")).isNotNull();
	}

	@Test
	void bookingStaffAdmitsByBookingReferenceWhenScanningFails() throws Exception {
		clock.set(START);
		Fixture fixture = createBooking("Reference Entry");
		String staffToken = accessToken("booking.staff", "StaffPassw0rd!");

		mockMvc.perform(post("/api/staff/admissions")
				.header("Authorization", "Bearer " + staffToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"bookingReference\":\"  %s  \"}".formatted(fixture.bookingReference().toLowerCase())))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.bookingReference").value(fixture.bookingReference()))
			.andExpect(jsonPath("$.seats.length()").value(2));

		assertThat(admissionCount(fixture.bookingId())).isOne();
	}

	@Test
	void aRepeatedAttemptReportsAlreadyAdmittedWithoutAnotherAdmission() throws Exception {
		clock.set(START);
		Fixture fixture = createBooking("Repeat");
		String staffToken = accessToken("booking.staff", "StaffPassw0rd!");

		admit(staffToken, "{\"admissionToken\":\"%s\"}".formatted(fixture.admissionToken()))
			.andExpect(status().isCreated());

		admit(staffToken, "{\"admissionToken\":\"%s\"}".formatted(fixture.admissionToken()))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("admission.already_admitted"));
		admit(staffToken, "{\"bookingReference\":\"%s\"}".formatted(fixture.bookingReference()))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("admission.already_admitted"));

		assertThat(admissionCount(fixture.bookingId())).isOne();
		Integer auditCount = jdbcTemplate.queryForObject(
				"select count(*) from cineflow.audit_events where action = 'BOOKING_ADMITTED' and subject_id = ?",
				Integer.class,
				fixture.bookingReference());
		assertThat(auditCount).isOne();
	}

	@Test
	void unknownTokensAndReferencesReturnOneStableSafeError() throws Exception {
		clock.set(START);
		createBooking("Unknown");
		String staffToken = accessToken("booking.staff", "StaffPassw0rd!");

		admit(staffToken, "{\"admissionToken\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}")
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("admission.not_found"))
			.andExpect(jsonPath("$.title").value("Booking not found"));
		admit(staffToken, "{\"bookingReference\":\"ZZZZZZZZZZ\"}")
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("admission.not_found"))
			.andExpect(jsonPath("$.title").value("Booking not found"));
	}

	@Test
	void admissionRequiresAnAuthenticatedBookingStaffMember() throws Exception {
		clock.set(START);
		Fixture fixture = createBooking("Roles");
		String adminToken = accessToken("administrator", "AdminPassw0rd!");
		String body = "{\"admissionToken\":\"%s\"}".formatted(fixture.admissionToken());

		mockMvc.perform(post("/api/staff/admissions")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("auth.unauthorized"));

		admit(adminToken, body)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));

		assertThat(admissionCount(fixture.bookingId())).isZero();
	}

	@Test
	void exactlyOneOfAdmissionTokenOrBookingReferenceIsAccepted() throws Exception {
		clock.set(START);
		Fixture fixture = createBooking("Ambiguous");
		String staffToken = accessToken("booking.staff", "StaffPassw0rd!");

		admit(staffToken, "{}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("request.invalid"));
		admit(staffToken, "{\"admissionToken\":\"%s\",\"bookingReference\":\"%s\"}"
				.formatted(fixture.admissionToken(), fixture.bookingReference()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("request.invalid"));
		admit(staffToken, "{\"admissionToken\":\"   \"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("request.invalid"));

		assertThat(admissionCount(fixture.bookingId())).isZero();
	}

	@Test
	void aMalformedBodyIsRejectedSafely() throws Exception {
		clock.set(START);
		String staffToken = accessToken("booking.staff", "StaffPassw0rd!");

		mockMvc.perform(post("/api/staff/admissions")
				.header("Authorization", "Bearer " + staffToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{not json"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void concurrentAdmissionsRecordExactlyOneAdmission() throws Exception {
		clock.set(START);
		Fixture fixture = createBooking("Concurrent Admission");
		long staffId = staffId("booking.staff");
		AdmitBookingRequest byToken = new AdmitBookingRequest(fixture.admissionToken(), null);
		AdmitBookingRequest byReference = new AdmitBookingRequest(null, fixture.bookingReference());

		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			List<Future<Object>> attempts = List.of(
					executor.submit(() -> admitAfter(start, staffId, byToken)),
					executor.submit(() -> admitAfter(start, staffId, byReference)));
			start.countDown();
			List<Object> results = new ArrayList<>();
			for (Future<Object> attempt : attempts) {
				results.add(attempt.get());
			}

			assertThat(results).filteredOn(AdmissionResponse.class::isInstance).hasSize(1);
			assertThat(results)
				.filteredOn(AdmissionException.class::isInstance)
				.singleElement()
				.extracting(result -> ((AdmissionException) result).code())
				.isEqualTo("admission.already_admitted");
		}
		finally {
			executor.shutdownNow();
		}
		assertThat(admissionCount(fixture.bookingId())).isOne();
	}

	private Object admitAfter(CountDownLatch start, long staffId, AdmitBookingRequest request) {
		try {
			start.await();
			return admission.admit(staffId, request);
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return exception;
		}
		catch (RuntimeException exception) {
			return exception;
		}
	}

	private ResultActions admit(String staffToken, String body) throws Exception {
		return mockMvc.perform(post("/api/staff/admissions")
			.header("Authorization", "Bearer " + staffToken)
			.contentType(MediaType.APPLICATION_JSON)
			.content(body));
	}

	private int admissionCount(long bookingId) {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from cineflow.admissions where booking_id = ?",
				Integer.class,
				bookingId);
		return count == null ? 0 : count;
	}

	private long staffId(String username) {
		Long id = jdbcTemplate.queryForObject(
				"select id from cineflow.staff_accounts where username = ?",
				Long.class,
				username);
		if (id == null) {
			throw new IllegalStateException("No Staff account " + username);
		}
		return id;
	}

	private String cachedStaffToken;

	private String cachedAdminToken;

	private String accessToken(String username, String password) throws Exception {
		if ("administrator".equals(username) && cachedAdminToken != null) {
			return cachedAdminToken;
		}
		if ("booking.staff".equals(username) && cachedStaffToken != null) {
			return cachedStaffToken;
		}
		MvcResult login = mockMvc.perform(post("/api/auth/login")
				.header("X-Forwarded-For", "198.51.100.72")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
			.andExpect(status().isOk())
			.andReturn();
		String token = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
		if ("administrator".equals(username)) {
			cachedAdminToken = token;
		}
		if ("booking.staff".equals(username)) {
			cachedStaffToken = token;
		}
		return token;
	}

	private record Fixture(
			long bookingId,
			String hallName,
			String bookingReference,
			String admissionToken) {
	}

	private Fixture createBooking(String movieTitle) {
		int hallId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.halls (name, row_count, seats_per_row, seat_map_locked, created_at)
						values (?, 1, 2, false, ?)
						returning id
						""",
				Integer.class,
				"Admit " + UUID.randomUUID(),
				Timestamp.from(START));
		jdbcTemplate.update(
				"""
						insert into cineflow.seats (hall_id, row_label, seat_number, disabled)
						values (?, 'A', 1, false), (?, 'A', 2, false)
						""",
				hallId,
				hallId);
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		List<Integer> seatIds = jdbcTemplate.queryForList(
				"select id from cineflow.seats where hall_id = ? order by row_label, seat_number",
				Integer.class,
				hallId);
		long movieId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.movies (
						    title, synopsis, genre, runtime_minutes, age_rating, poster_url,
						    source_provider, external_id, source_refreshed_at)
						values (?, 'Synopsis', 'Adventure', 90, 'PG', null, 'fixture', ?, ?)
						returning id
						""",
				Long.class,
				movieTitle,
				UUID.randomUUID().toString(),
				Timestamp.from(START));
		long showtimeId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
						values (?, ?, '2099-06-20T11:30:00Z', 28.00, 18.00)
						returning id
						""",
				Long.class,
				hallId,
				movieId);
		String bookingReference = "REF" + UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
		String admissionToken = UUID.randomUUID().toString().replace("-", "")
				+ UUID.randomUUID().toString().replace("-", "").substring(0, 11);
		long bookingId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.bookings (showtime_id, email, booking_reference, admission_token_hash, created_at)
						values (?, ?, ?, ?, ?)
						returning id
						""",
				Long.class,
				showtimeId,
				"aisyah@example.com",
				bookingReference,
				Sha256.hash(admissionToken),
				Timestamp.from(START));
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (
						    showtime_id, hall_id, seat_id, claim_kind, expires_at, booking_id, ticket_type, price_myr)
						values (?, ?, ?, 'BOOKING', null, ?, 'ADULT', 28.00)
						""",
				showtimeId,
				hallId,
				seatIds.get(0),
				bookingId);
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (
						    showtime_id, hall_id, seat_id, claim_kind, expires_at, booking_id, ticket_type, price_myr)
						values (?, ?, ?, 'BOOKING', null, ?, 'CHILD', 18.00)
						""",
				showtimeId,
				hallId,
				seatIds.get(1),
				bookingId);
		String hallName = jdbcTemplate.queryForObject(
				"select name from cineflow.halls where id = ?",
				String.class,
				hallId);
		return new Fixture(bookingId, hallName, bookingReference, admissionToken);
	}

	@TestConfiguration
	static class ClockConfig {

		@Bean
		@Primary
		MutableClock mutableClock() {
			return new MutableClock(START);
		}
	}
}
