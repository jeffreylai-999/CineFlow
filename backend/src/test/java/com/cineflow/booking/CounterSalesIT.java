package com.cineflow.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import com.jayway.jsonpath.JsonPath;

@Import({ TestcontainersConfiguration.class, CounterSalesIT.ClockConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CounterSalesIT {

	private static final Instant START = Instant.parse("2026-09-16T00:00:00Z");
	private static final Instant FUTURE_START = Instant.parse("2099-06-20T11:30:00Z");

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MutableClock clock;

	@Test
	void bookingStaffRecordsACashSaleWithoutWalkInCustomerDetails() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("Cash " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Cash Sale", 90), FUTURE_START);
		int[] seats = seatIds(hallId);

		mockMvc.perform(get("/api/staff/showtimes").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.movieTitle=='Cash Sale')].id").value(hasItem((int) showtimeId)))
			.andExpect(jsonPath("$[?(@.movieTitle=='Cash Sale')].counterSalesOpen").value(hasItem(true)))
			.andExpect(jsonPath("$[?(@.movieTitle=='Cash Sale')].adultPriceMyr").value(hasItem(28.00)))
			.andExpect(jsonPath("$[?(@.movieTitle=='Cash Sale')].childPriceMyr").value(hasItem(18.00)));

		mockMvc.perform(get("/api/staff/showtimes/" + showtimeId + "/seats").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.showtimeId").value(showtimeId))
			.andExpect(jsonPath("$.movieTitle").value("Cash Sale"))
			.andExpect(jsonPath("$.bookingLimit").value(10))
			.andExpect(jsonPath("$.counterSalesOpen").value(true))
			.andExpect(jsonPath("$.seats[0].state").value("AVAILABLE"))
			.andExpect(jsonPath("$.seats[1].state").value("AVAILABLE"));

		String holdId = createCounterHold(showtimeId, token, seats);
		String idempotencyKey = UUID.randomUUID().toString();

		MvcResult confirmed = confirm(
				showtimeId,
				token,
				holdId,
				"CASH",
				idempotencyKey,
				ticket(seats[0], "ADULT") + "," + ticket(seats[1], "CHILD"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.showtimeId").value(showtimeId))
			.andExpect(jsonPath("$.movieTitle").value("Cash Sale"))
			.andExpect(jsonPath("$.hallName").isString())
			.andExpect(jsonPath("$.startsAtCinemaTime").value("2099-06-20T19:30:00"))
			.andExpect(jsonPath("$.timeZone").value("Asia/Kuala_Lumpur"))
			.andExpect(jsonPath("$.email").doesNotExist())
			.andExpect(jsonPath("$.seats[0].label").value("A1"))
			.andExpect(jsonPath("$.seats[0].ticketType").value("ADULT"))
			.andExpect(jsonPath("$.seats[0].priceMyr").value(28.00))
			.andExpect(jsonPath("$.seats[1].label").value("A2"))
			.andExpect(jsonPath("$.seats[1].ticketType").value("CHILD"))
			.andExpect(jsonPath("$.seats[1].priceMyr").value(18.00))
			.andExpect(jsonPath("$.totalMyr").value(46.00))
			.andReturn();

		String body = confirmed.getResponse().getContentAsString();
		String bookingReference = JsonPath.read(body, "$.bookingReference");
		String admissionToken = JsonPath.read(body, "$.admissionToken");
		assertThat(bookingReference).hasSize(10);
		assertThat(admissionToken).hasSize(43);

		var booking = jdbcTemplate.queryForMap(
				"select id, email, booking_reference from cineflow.bookings where showtime_id = ?",
				showtimeId);
		assertThat(booking.get("email")).isNull();
		assertThat(booking.get("booking_reference")).isEqualTo(bookingReference);
		long bookingId = ((Number) booking.get("id")).longValue();

		var payment = jdbcTemplate.queryForMap(
				"select method, amount_myr, idempotency_key from cineflow.payments where booking_id = ?",
				bookingId);
		assertThat(payment.get("method")).isEqualTo("CASH");
		assertThat(payment.get("amount_myr").toString()).isEqualTo("46.00");
		assertThat(payment.get("idempotency_key")).isEqualTo(idempotencyKey);

		var claims = jdbcTemplate.queryForList(
				"""
						select claim_kind, expires_at, hold_id, booking_id, ticket_type, price_myr
						from cineflow.seat_claims
						where showtime_id = ?
						order by seat_id
						""",
				showtimeId);
		assertThat(claims).hasSize(2);
		assertThat(claims.get(0).get("claim_kind")).isEqualTo("BOOKING");
		assertThat(claims.get(0).get("expires_at")).isNull();
		assertThat(claims.get(0).get("hold_id")).isNull();
		assertThat(((Number) claims.get(0).get("booking_id")).longValue()).isEqualTo(bookingId);
		assertThat(claims.get(0).get("ticket_type")).isEqualTo("ADULT");
		assertThat(claims.get(1).get("ticket_type")).isEqualTo("CHILD");

		long staffId = jdbcTemplate.queryForObject(
				"select id from cineflow.staff_accounts where username = 'booking.staff'",
				Long.class);
		var events = jdbcTemplate.queryForList(
				"""
						select actor_staff_id, subject_type, subject_id
						from cineflow.audit_events
						where action = 'STAFF_ASSISTED_BOOKING_CREATED' and subject_id = ?
						""",
				bookingReference);
		assertThat(events).hasSize(1);
		assertThat(((Number) events.getFirst().get("actor_staff_id")).longValue()).isEqualTo(staffId);
		assertThat(events.getFirst().get("subject_type")).isEqualTo("booking");

		mockMvc.perform(get("/api/staff/showtimes/" + showtimeId + "/seats").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.seats[0].state").value("BOOKED"))
			.andExpect(jsonPath("$.seats[1].state").value("BOOKED"));
	}

	@Test
	void bookingStaffRecordsAReceivedCardPaymentWithoutCardCredentials() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("Card " + UUID.randomUUID(), 1, 1);
		long showtimeId = insertShowtime(hallId, insertMovie("Card Sale", 90), FUTURE_START);
		int seatId = seatIds(hallId)[0];
		String holdId = createCounterHold(showtimeId, token, seatId);

		confirm(showtimeId, token, holdId, "CARD", UUID.randomUUID().toString(), ticket(seatId, "ADULT"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.totalMyr").value(28.00));

		var payment = jdbcTemplate.queryForMap(
				"""
						select p.method, p.amount_myr
						from cineflow.payments p
						join cineflow.bookings b on b.id = p.booking_id
						where b.showtime_id = ?
						""",
				showtimeId);
		assertThat(payment.get("method")).isEqualTo("CARD");
		assertThat(payment.get("amount_myr").toString()).isEqualTo("28.00");
	}

	@Test
	void counterSalesContinueAfterTheOnlineBookingCutoff() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("Late " + UUID.randomUUID(), 1, 2);
		long movieId = insertMovie("Late Counter Sale", 90);
		long showtimeId = insertShowtime(hallId, movieId, START.plus(Duration.ofMinutes(10)));
		int seatId = seatIds(hallId)[0];

		mockMvc.perform(post("/api/showtimes/" + showtimeId + "/holds")
				.header("X-Forwarded-For", "online-" + UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"seatIds\":[" + seatId + "]}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.cutoff"));

		String holdId = createCounterHold(showtimeId, token, seatId);
		confirm(showtimeId, token, holdId, "CASH", UUID.randomUUID().toString(), ticket(seatId, "ADULT"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.totalMyr").value(28.00));
	}

	@Test
	void counterSalesFailAfterTheCounterSalesCutoff() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("Cutoff " + UUID.randomUUID(), 1, 2);
		long movieId = insertMovie("Counter Cutoff", 90);
		long showtimeId = insertShowtime(hallId, movieId, START.plus(Duration.ofMinutes(10)));
		int[] seats = seatIds(hallId);
		String holdId = createCounterHold(showtimeId, token, seats[0]);

		clock.set(START.plus(Duration.ofMinutes(26)));
		String lateToken = staffToken();

		confirm(showtimeId, lateToken, holdId, "CASH", UUID.randomUUID().toString(), ticket(seats[0], "ADULT"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.counter_sales_cutoff"));

		mockMvc.perform(post("/api/staff/showtimes/" + showtimeId + "/holds")
				.header("Authorization", "Bearer " + lateToken)
				.header("X-Forwarded-For", "hold-" + UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"seatIds\":[" + seats[1] + "]}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.counter_sales_cutoff"));

		mockMvc.perform(get("/api/staff/showtimes/" + showtimeId + "/seats").header("Authorization", "Bearer " + lateToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.counterSalesOpen").value(false));

		mockMvc.perform(get("/api/staff/showtimes").header("Authorization", "Bearer " + lateToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.movieTitle=='Counter Cutoff')]").isEmpty());

		assertThat(bookingCount(showtimeId)).isZero();
		assertThat(paymentCount(showtimeId)).isZero();
	}

	@Test
	void staffSeatMapDistinguishesHoldBookingAndDisabledStates() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("States " + UUID.randomUUID(), 1, 5);
		long showtimeId = insertShowtime(hallId, insertMovie("State Map", 90), FUTURE_START);
		int[] seats = seatIds(hallId);
		jdbcTemplate.update("update cineflow.seats set disabled = true where id = ?", seats[1]);
		insertHoldClaim(showtimeId, hallId, seats[2], Timestamp.from(START.plusSeconds(600)));
		insertBookingClaim(showtimeId, hallId, seats[3]);
		insertHoldClaim(showtimeId, hallId, seats[4], Timestamp.from(START.minusSeconds(1)));

		mockMvc.perform(get("/api/staff/showtimes/" + showtimeId + "/seats").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.seats.length()").value(5))
			.andExpect(jsonPath("$.seats[0].label").value("A1"))
			.andExpect(jsonPath("$.seats[0].state").value("AVAILABLE"))
			.andExpect(jsonPath("$.seats[1].label").value("A2"))
			.andExpect(jsonPath("$.seats[1].state").value("DISABLED"))
			.andExpect(jsonPath("$.seats[2].label").value("A3"))
			.andExpect(jsonPath("$.seats[2].state").value("HELD"))
			.andExpect(jsonPath("$.seats[3].label").value("A4"))
			.andExpect(jsonPath("$.seats[3].state").value("BOOKED"))
			.andExpect(jsonPath("$.seats[4].label").value("A5"))
			.andExpect(jsonPath("$.seats[4].state").value("AVAILABLE"));
	}

	@Test
	void counterSaleEndpointsRequireTheBookingStaffRole() throws Exception {
		clock.set(START);
		String adminToken = login("administrator", "AdminPassw0rd!");
		int hallId = createHall("Roles " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Role Gate", 90), FUTURE_START);
		int seatId = seatIds(hallId)[0];
		String confirmBody = """
				{"holdId":"%s","tickets":[%s],"method":"CASH","idempotencyKey":"%s"}
				""".formatted(UUID.randomUUID(), ticket(seatId, "ADULT"), UUID.randomUUID());

		mockMvc.perform(get("/api/staff/showtimes"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("auth.unauthorized"));
		mockMvc.perform(get("/api/staff/showtimes/" + showtimeId + "/seats"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/staff/showtimes/" + showtimeId + "/holds")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"seatIds\":[" + seatId + "]}"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/staff/showtimes/" + showtimeId + "/confirm")
				.contentType(MediaType.APPLICATION_JSON)
				.content(confirmBody))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/staff/showtimes").header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));
		mockMvc.perform(get("/api/staff/showtimes/" + showtimeId + "/seats")
				.header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));
		mockMvc.perform(post("/api/staff/showtimes/" + showtimeId + "/holds")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"seatIds\":[" + seatId + "]}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));
		mockMvc.perform(post("/api/staff/showtimes/" + showtimeId + "/confirm")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(confirmBody))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));

		mockMvc.perform(get("/api/showtimes/" + showtimeId + "/seats"))
			.andExpect(status().isOk());

		assertThat(bookingCount(showtimeId)).isZero();
	}

	@Test
	void counterSalesObeyTheBookingLimit() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("Limit " + UUID.randomUUID(), 1, 11);
		long showtimeId = insertShowtime(hallId, insertMovie("Limit Gate", 90), FUTURE_START);
		int[] seats = seatIds(hallId);

		mockMvc.perform(get("/api/staff/showtimes/" + showtimeId + "/seats").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.bookingLimit").value(10));

		mockMvc.perform(post("/api/staff/showtimes/" + showtimeId + "/holds")
				.header("Authorization", "Bearer " + token)
				.header("X-Forwarded-For", "hold-" + UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"seatIds\":[" + join(seats) + "]}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.limit"));
	}

	@Test
	void counterSaleConfirmationIsIdempotent() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("Replay " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Counter Replay", 90), FUTURE_START);
		int seatId = seatIds(hallId)[0];
		String holdId = createCounterHold(showtimeId, token, seatId);
		String idempotencyKey = UUID.randomUUID().toString();

		MvcResult first = confirm(showtimeId, token, holdId, "CASH", idempotencyKey, ticket(seatId, "CHILD"))
			.andExpect(status().isCreated())
			.andReturn();
		String bookingReference = JsonPath.read(first.getResponse().getContentAsString(), "$.bookingReference");

		confirm(showtimeId, token, holdId, "CASH", idempotencyKey, ticket(seatId, "CHILD"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.bookingReference").value(bookingReference))
			.andExpect(jsonPath("$.totalMyr").value(18.00))
			.andExpect(jsonPath("$.seats[0].label").value("A1"))
			.andExpect(jsonPath("$.admissionToken").doesNotExist());

		assertThat(bookingCount(showtimeId)).isOne();
		assertThat(paymentCount(showtimeId)).isOne();
	}

	@Test
	void aReusedCounterSaleKeyWithADifferentRequestConflicts() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("Fingerprint " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Counter Fingerprint", 90), FUTURE_START);
		int[] seats = seatIds(hallId);
		String idempotencyKey = UUID.randomUUID().toString();

		String firstHold = createCounterHold(showtimeId, token, seats[0]);
		confirm(showtimeId, token, firstHold, "CASH", idempotencyKey, ticket(seats[0], "ADULT"))
			.andExpect(status().isCreated());

		String secondHold = createCounterHold(showtimeId, token, seats[1]);
		confirm(showtimeId, token, secondHold, "CARD", idempotencyKey, ticket(seats[1], "ADULT"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.idempotency_conflict"));

		assertThat(bookingCount(showtimeId)).isOne();
		assertThat(paymentCount(showtimeId)).isOne();
	}

	@Test
	void counterSaleCannotClaimAnAlreadyBookedSeat() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("Conflict " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Counter Conflict", 90), FUTURE_START);
		int seatId = seatIds(hallId)[0];

		String firstHold = createCounterHold(showtimeId, token, seatId);
		confirm(showtimeId, token, firstHold, "CASH", UUID.randomUUID().toString(), ticket(seatId, "ADULT"))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/staff/showtimes/" + showtimeId + "/holds")
				.header("Authorization", "Bearer " + token)
				.header("X-Forwarded-For", "hold-" + UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"seatIds\":[" + seatId + "]}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.seats_unavailable"));
	}

	@Test
	void counterSaleWithAnExpiredHoldFails() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("Expired " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Counter Expired Hold", 90), FUTURE_START);
		int seatId = seatIds(hallId)[0];
		String holdId = createCounterHold(showtimeId, token, seatId);

		clock.set(START.plusSeconds(600));
		confirm(showtimeId, token, holdId, "CASH", UUID.randomUUID().toString(), ticket(seatId, "ADULT"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.hold_expired"));

		assertThat(bookingCount(showtimeId)).isZero();
		assertThat(paymentCount(showtimeId)).isZero();
	}

	@Test
	void counterSaleTicketsMustCoverExactlyTheHeldSeats() throws Exception {
		clock.set(START);
		String token = staffToken();
		int hallId = createHall("Mismatch " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Counter Mismatch", 90), FUTURE_START);
		int[] seats = seatIds(hallId);
		String holdId = createCounterHold(showtimeId, token, seats);

		confirm(showtimeId, token, holdId, "CASH", UUID.randomUUID().toString(), ticket(seats[0], "ADULT"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("booking.invalid_seat_selection"));

		assertThat(bookingCount(showtimeId)).isZero();
		assertThat(paymentCount(showtimeId)).isZero();
	}

	@Test
	void anUnknownShowtimeIsNotFoundForStaff() throws Exception {
		clock.set(START);
		String token = staffToken();

		mockMvc.perform(get("/api/staff/showtimes/999999/seats").header("Authorization", "Bearer " + token))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("booking.showtime_not_found"));

		mockMvc.perform(post("/api/staff/showtimes/999999/holds")
				.header("Authorization", "Bearer " + token)
				.header("X-Forwarded-For", "hold-" + UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"seatIds\":[1]}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("booking.showtime_not_found"));
	}

	private ResultActions confirm(
			long showtimeId,
			String token,
			String holdId,
			String method,
			String idempotencyKey,
			String ticketsJson) throws Exception {
		return mockMvc.perform(post("/api/staff/showtimes/" + showtimeId + "/confirm")
			.header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{
					  "holdId": "%s",
					  "tickets": [%s],
					  "method": "%s",
					  "idempotencyKey": "%s"
					}
					""".formatted(holdId, ticketsJson, method, idempotencyKey)));
	}

	private String createCounterHold(long showtimeId, String token, int... seatIds) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/staff/showtimes/" + showtimeId + "/holds")
				.header("Authorization", "Bearer " + token)
				.header("X-Forwarded-For", "hold-" + UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"seatIds\":[" + join(seatIds) + "]}"))
			.andExpect(status().isCreated())
			.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.holdId");
	}

	private static String ticket(int seatId, String ticketType) {
		return "{\"seatId\": %d, \"ticketType\": \"%s\"}".formatted(seatId, ticketType);
	}

	private static String join(int... seatIds) {
		StringBuilder selection = new StringBuilder();
		for (int index = 0; index < seatIds.length; index++) {
			if (index > 0) {
				selection.append(',');
			}
			selection.append(seatIds[index]);
		}
		return selection.toString();
	}

	private String staffToken() throws Exception {
		return login("booking.staff", "StaffPassw0rd!");
	}

	private String login(String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.header("X-Forwarded-For", "login-" + UUID.randomUUID())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"%s"}
						""".formatted(username, password)))
			.andExpect(status().isOk())
			.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}

	private int bookingCount(long showtimeId) {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from cineflow.bookings where showtime_id = ?",
				Integer.class,
				showtimeId);
		return count == null ? 0 : count;
	}

	private int paymentCount(long showtimeId) {
		Integer count = jdbcTemplate.queryForObject(
				"""
						select count(*)
						from cineflow.payments p
						join cineflow.bookings b on b.id = p.booking_id
						where b.showtime_id = ?
						""",
				Integer.class,
				showtimeId);
		return count == null ? 0 : count;
	}

	private void insertHoldClaim(long showtimeId, int hallId, int seatId, Timestamp expiresAt) {
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (showtime_id, hall_id, seat_id, claim_kind, expires_at)
						values (?, ?, ?, 'HOLD', ?)
						""",
				showtimeId,
				hallId,
				seatId,
				expiresAt);
	}

	private void insertBookingClaim(long showtimeId, int hallId, int seatId) {
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (showtime_id, hall_id, seat_id, claim_kind, expires_at)
						values (?, ?, ?, 'BOOKING', null)
						""",
				showtimeId,
				hallId,
				seatId);
	}

	private int createHall(String name, int rows, int seatsPerRow) {
		int hallId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.halls (name, row_count, seats_per_row, seat_map_locked, created_at)
						values (?, ?, ?, false, ?)
						returning id
						""",
				Integer.class,
				name,
				rows,
				seatsPerRow,
				Timestamp.from(START));
		for (int row = 0; row < rows; row++) {
			String rowLabel = String.valueOf((char) ('A' + row));
			for (int seatNumber = 1; seatNumber <= seatsPerRow; seatNumber++) {
				jdbcTemplate.update(
						"insert into cineflow.seats (hall_id, row_label, seat_number, disabled) values (?, ?, ?, false)",
						hallId,
						rowLabel,
						seatNumber);
			}
		}
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		return hallId;
	}

	private int[] seatIds(int hallId) {
		return jdbcTemplate.queryForList(
				"select id from cineflow.seats where hall_id = ? order by row_label, seat_number",
				Integer.class,
				hallId)
			.stream()
			.mapToInt(Integer::intValue)
			.toArray();
	}

	private long insertMovie(String title, int runtimeMinutes) {
		return jdbcTemplate.queryForObject(
				"""
						insert into cineflow.movies (
						    title, synopsis, genre, runtime_minutes, age_rating, poster_url,
						    source_provider, external_id, source_refreshed_at)
						values (?, 'Synopsis', 'Adventure', ?, 'PG', null, 'fixture', ?, ?)
						returning id
						""",
				Long.class,
				title,
				runtimeMinutes,
				UUID.randomUUID().toString(),
				Timestamp.from(START));
	}

	private long insertShowtime(int hallId, long movieId, Instant startsAt) {
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

	@TestConfiguration
	static class ClockConfig {

		@Bean
		@Primary
		MutableClock mutableClock() {
			return new MutableClock(START);
		}
	}
}
