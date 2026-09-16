package com.cineflow.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
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

import com.cineflow.MutableClock;
import com.cineflow.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;

@Import({ TestcontainersConfiguration.class, CheckoutIT.ClockConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
class CheckoutIT {

	private static final Instant START = Instant.parse("2026-09-16T00:00:00Z");
	private static final String SUCCESS_CARD = "4242424242424242";
	private static final String DECLINED_CARD = "4000000000000002";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MutableClock clock;

	@Autowired
	Booking booking;

	@Test
	void onlineCustomerConfirmsBookingThroughSimulatedPayment() throws Exception {
		clock.set(START);
		int hallId = createHall("Checkout " + UUID.randomUUID(), 1, 2);
		long movieId = insertMovie("Checkout Gate", 90);
		long showtimeId = insertShowtime(hallId, movieId);
		int[] seats = seatIds(hallId);
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seats);
		String idempotencyKey = UUID.randomUUID().toString();

		MvcResult result = checkout(
				showtimeId,
				holdId,
				"Aisyah@Example.com",
				SUCCESS_CARD,
				idempotencyKey,
				ticket(seats[0], "ADULT") + "," + ticket(seats[1], "CHILD"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.showtimeId").value(showtimeId))
			.andExpect(jsonPath("$.movieTitle").value("Checkout Gate"))
			.andExpect(jsonPath("$.startsAtCinemaTime").value("2099-06-20T19:30:00"))
			.andExpect(jsonPath("$.timeZone").value("Asia/Kuala_Lumpur"))
			.andExpect(jsonPath("$.email").value("aisyah@example.com"))
			.andExpect(jsonPath("$.seats[0].label").value("A1"))
			.andExpect(jsonPath("$.seats[0].ticketType").value("ADULT"))
			.andExpect(jsonPath("$.seats[0].priceMyr").value(28.00))
			.andExpect(jsonPath("$.seats[1].label").value("A2"))
			.andExpect(jsonPath("$.seats[1].ticketType").value("CHILD"))
			.andExpect(jsonPath("$.seats[1].priceMyr").value(18.00))
			.andExpect(jsonPath("$.totalMyr").value(46.00))
			.andReturn();

		String body = result.getResponse().getContentAsString();
		String bookingReference = JsonPath.read(body, "$.bookingReference");
		String admissionToken = JsonPath.read(body, "$.admissionToken");
		assertThat(bookingReference).hasSize(10).doesNotContain("0", "1", "I", "L", "O");
		assertThat(admissionToken).hasSize(43);

		var booking = jdbcTemplate.queryForMap(
				"select id, email, booking_reference, admission_token_hash from cineflow.bookings where showtime_id = ?",
				showtimeId);
		assertThat(booking.get("email")).isEqualTo("aisyah@example.com");
		assertThat(booking.get("booking_reference")).isEqualTo(bookingReference);
		assertThat(booking.get("admission_token_hash")).isEqualTo(sha256(admissionToken));
		long bookingId = ((Number) booking.get("id")).longValue();

		var payment = jdbcTemplate.queryForMap(
				"select booking_id, amount_myr, method, idempotency_key from cineflow.payments where booking_id = ?",
				bookingId);
		assertThat(((Number) payment.get("booking_id")).longValue()).isEqualTo(bookingId);
		assertThat(payment.get("amount_myr").toString()).isEqualTo("46.00");
		assertThat(payment.get("method")).isEqualTo("CARD_SIMULATED");
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
		assertThat(claims.get(0).get("price_myr").toString()).isEqualTo("28.00");
		assertThat(claims.get(1).get("ticket_type")).isEqualTo("CHILD");
		assertThat(claims.get(1).get("price_myr").toString()).isEqualTo("18.00");
	}

	private org.springframework.test.web.servlet.ResultActions checkout(
			long showtimeId,
			String holdId,
			String email,
			String cardNumber,
			String idempotencyKey,
			String ticketsJson) throws Exception {
		return checkout(
				showtimeId,
				holdId,
				email,
				cardNumber,
				idempotencyKey,
				ticketsJson,
				"test-" + UUID.randomUUID());
	}

	private org.springframework.test.web.servlet.ResultActions checkout(
			long showtimeId,
			String holdId,
			String email,
			String cardNumber,
			String idempotencyKey,
			String ticketsJson,
			String clientAddress) throws Exception {
		var request = post("/api/showtimes/" + showtimeId + "/checkout")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "holdId": "%s",
						  "email": "%s",
						  "tickets": [%s],
						  "cardNumber": "%s",
						  "idempotencyKey": "%s"
						}
						""".formatted(holdId, email, ticketsJson, cardNumber, idempotencyKey));
		if (clientAddress != null) {
			request.header("X-Forwarded-For", clientAddress);
		}
		return mockMvc.perform(request);
	}

	private static String ticket(int seatId, String ticketType) {
		return "{\"seatId\": %d, \"ticketType\": \"%s\"}".formatted(seatId, ticketType);
	}

	@Test
	void aDeclinedPaymentLeavesTheSeatHoldActiveWithoutExtendingIt() throws Exception {
		clock.set(START);
		int hallId = createHall("Declined " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Declined Payment", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seatId);

		checkout(showtimeId, holdId, "aisyah@example.com", DECLINED_CARD, UUID.randomUUID().toString(),
				ticket(seatId, "ADULT"))
			.andExpect(status().isPaymentRequired())
			.andExpect(jsonPath("$.code").value("booking.payment_declined"));

		assertThat(bookingCount(showtimeId)).isZero();
		assertThat(paymentCount(showtimeId)).isZero();
		var hold = jdbcTemplate.queryForMap(
				"select expires_at from cineflow.seat_holds where id = ?",
				UUID.fromString(holdId));
		assertThat((Timestamp) hold.get("expires_at")).isEqualTo(Timestamp.from(START.plusSeconds(600)));
		var claim = jdbcTemplate.queryForMap(
				"""
						select claim_kind, expires_at, hold_id, booking_id
						from cineflow.seat_claims
						where showtime_id = ? and seat_id = ?
						""",
				showtimeId,
				seatId);
		assertThat(claim.get("claim_kind")).isEqualTo("HOLD");
		assertThat((Timestamp) claim.get("expires_at")).isEqualTo(Timestamp.from(START.plusSeconds(600)));
		assertThat(claim.get("hold_id")).isEqualTo(UUID.fromString(holdId));
		assertThat(claim.get("booking_id")).isNull();
	}

	@Test
	void aFailedPaymentCanBeRetriedWithinTheSameSeatHold() throws Exception {
		clock.set(START);
		int hallId = createHall("Retry " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Retry Payment", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seatId);

		checkout(showtimeId, holdId, "aisyah@example.com", DECLINED_CARD, UUID.randomUUID().toString(),
				ticket(seatId, "ADULT"))
			.andExpect(status().isPaymentRequired());

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, UUID.randomUUID().toString(),
				ticket(seatId, "ADULT"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.totalMyr").value(28.00));

		assertThat(bookingCount(showtimeId)).isOne();
		assertThat(paymentCount(showtimeId)).isOne();
	}

	@Test
	void anIdempotentReplayReturnsTheSameBookingWithoutDuplicatingIt() throws Exception {
		clock.set(START);
		int hallId = createHall("Replay " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Idempotent Replay", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seatId);
		String idempotencyKey = UUID.randomUUID().toString();

		MvcResult first = checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, idempotencyKey,
				ticket(seatId, "CHILD"))
			.andExpect(status().isCreated())
			.andReturn();
		String bookingReference = JsonPath.read(first.getResponse().getContentAsString(), "$.bookingReference");

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, idempotencyKey,
				ticket(seatId, "CHILD"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.bookingReference").value(bookingReference))
			.andExpect(jsonPath("$.totalMyr").value(18.00))
			.andExpect(jsonPath("$.seats[0].label").value("A1"))
			.andExpect(jsonPath("$.admissionToken").doesNotExist());

		assertThat(bookingCount(showtimeId)).isOne();
		assertThat(paymentCount(showtimeId)).isOne();
	}

	@Test
	void anExpiredSeatHoldCannotCheckout() throws Exception {
		clock.set(START);
		int hallId = createHall("Expired " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Expired Hold Checkout", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seatId);

		clock.set(START.plusSeconds(600));
		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, UUID.randomUUID().toString(),
				ticket(seatId, "ADULT"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.hold_expired"));

		assertThat(bookingCount(showtimeId)).isZero();
		assertThat(paymentCount(showtimeId)).isZero();
	}

	@Test
	void anUnknownSeatHoldIsNotFound() throws Exception {
		clock.set(START);
		int hallId = createHall("Unknown " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Unknown Hold", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);

		checkout(showtimeId, UUID.randomUUID().toString(), "aisyah@example.com", SUCCESS_CARD,
				UUID.randomUUID().toString(), ticket(seatId, "ADULT"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("booking.hold_not_found"));
	}

	@Test
	void aConsumedSeatHoldCannotCheckoutAgainWithANewKey() throws Exception {
		clock.set(START);
		int hallId = createHall("Consumed " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Consumed Hold", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seatId);

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, UUID.randomUUID().toString(),
				ticket(seatId, "ADULT"))
			.andExpect(status().isCreated());

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, UUID.randomUUID().toString(),
				ticket(seatId, "ADULT"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.hold_unavailable"));

		assertThat(bookingCount(showtimeId)).isOne();
		assertThat(paymentCount(showtimeId)).isOne();
	}

	@Test
	void ticketsMustCoverExactlyTheHeldSeats() throws Exception {
		clock.set(START);
		int hallId = createHall("Mismatch " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Ticket Mismatch", 90));
		int[] seats = seatIds(hallId);
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		int otherHallId = createHall("Other " + UUID.randomUUID(), 1, 2);
		int otherSeatId = seatIds(otherHallId)[0];
		String holdId = createHold(showtimeId, seats);

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, UUID.randomUUID().toString(),
				ticket(seats[0], "ADULT"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("booking.invalid_seat_selection"));

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, UUID.randomUUID().toString(),
				ticket(seats[0], "ADULT") + "," + ticket(otherSeatId, "CHILD"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("booking.invalid_seat_selection"));

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, UUID.randomUUID().toString(),
				ticket(seats[0], "ADULT") + "," + ticket(seats[0], "CHILD") + "," + ticket(seats[1], "CHILD"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("booking.invalid_seat_selection"));

		assertThat(bookingCount(showtimeId)).isZero();
		assertThat(paymentCount(showtimeId)).isZero();
	}

	@Test
	void anInvalidEmailIsRejectedBeforePayment() throws Exception {
		clock.set(START);
		int hallId = createHall("Email " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Invalid Email", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seatId);

		checkout(showtimeId, holdId, "not-an-email", SUCCESS_CARD, UUID.randomUUID().toString(),
				ticket(seatId, "ADULT"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("request.invalid"));

		assertThat(bookingCount(showtimeId)).isZero();
		assertThat(paymentCount(showtimeId)).isZero();
	}

	@Test
	void theTotalUsesTheStoredTicketPrices() throws Exception {
		clock.set(START);
		int hallId = createHall("Prices " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Stored Prices", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seatId);
		jdbcTemplate.update("update cineflow.showtimes set adult_price_myr = 33.50 where id = ?", showtimeId);

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, UUID.randomUUID().toString(),
				ticket(seatId, "ADULT"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.seats[0].priceMyr").value(33.50))
			.andExpect(jsonPath("$.totalMyr").value(33.50));
	}

	@Test
	void checkoutIsRateLimitedPerClientAddress() throws Exception {
		clock.set(START);
		int hallId = createHall("Checkout limit " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Rate Limited Checkout", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String address = "checkout-" + UUID.randomUUID();

		for (int attempt = 0; attempt < 10; attempt++) {
			checkout(showtimeId, UUID.randomUUID().toString(), "aisyah@example.com", SUCCESS_CARD,
					UUID.randomUUID().toString(), ticket(seatId, "ADULT"), address)
				.andExpect(status().isNotFound());
		}

		checkout(showtimeId, UUID.randomUUID().toString(), "aisyah@example.com", SUCCESS_CARD,
				UUID.randomUUID().toString(), ticket(seatId, "ADULT"), address)
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("booking.rate_limited"));
	}

	@Test
	void concurrentCheckoutWithTheSameIdempotencyKeyConfirmsOnce() throws Exception {
		clock.set(START);
		int hallId = createHall("Concurrent " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Concurrent Checkout", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seatId);
		CheckoutRequest request = new CheckoutRequest(
				UUID.fromString(holdId),
				"aisyah@example.com",
				List.of(new CheckoutTicketRequest((long) seatId, TicketType.ADULT)),
				SUCCESS_CARD,
				UUID.randomUUID().toString());

		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			List<Future<CheckoutResult>> attempts = List.of(
					executor.submit(() -> checkoutAfter(start, showtimeId, request)),
					executor.submit(() -> checkoutAfter(start, showtimeId, request)));
			start.countDown();
			List<CheckoutResult> results = new ArrayList<>();
			for (Future<CheckoutResult> attempt : attempts) {
				results.add(attempt.get());
			}

			assertThat(results).filteredOn(CheckoutResult::replayed).hasSize(1);
			assertThat(results).filteredOn(result -> !result.replayed()).hasSize(1);
			assertThat(results.get(0).confirmation().bookingReference())
				.isEqualTo(results.get(1).confirmation().bookingReference());
			assertThat(paymentCount(showtimeId)).isOne();
		}
		finally {
			executor.shutdownNow();
		}
	}

	private CheckoutResult checkoutAfter(CountDownLatch start, long showtimeId, CheckoutRequest request)
			throws InterruptedException {
		start.await();
		return booking.checkout(showtimeId, request);
	}

	@Test
	void concurrentCheckoutWithTheSameKeyAcrossShowtimesConflictsCleanly() throws Exception {
		clock.set(START);
		int hallId = createHall("Race A " + UUID.randomUUID(), 1, 2);
		int otherHallId = createHall("Race B " + UUID.randomUUID(), 1, 2);
		long movieId = insertMovie("Cross Showtime Race", 90);
		long showtimeId = insertShowtime(hallId, movieId);
		long otherShowtimeId = insertShowtime(otherHallId, movieId);
		int seatId = seatIds(hallId)[0];
		int otherSeatId = seatIds(otherHallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id in (?, ?)", hallId, otherHallId);
		String idempotencyKey = UUID.randomUUID().toString();
		CheckoutRequest first = new CheckoutRequest(
				UUID.fromString(createHold(showtimeId, seatId)),
				"aisyah@example.com",
				List.of(new CheckoutTicketRequest((long) seatId, TicketType.ADULT)),
				SUCCESS_CARD,
				idempotencyKey);
		CheckoutRequest second = new CheckoutRequest(
				UUID.fromString(createHold(otherShowtimeId, otherSeatId)),
				"aisyah@example.com",
				List.of(new CheckoutTicketRequest((long) otherSeatId, TicketType.ADULT)),
				SUCCESS_CARD,
				idempotencyKey);

		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			List<Future<Object>> attempts = List.of(
					executor.submit(() -> checkoutRace(start, showtimeId, first)),
					executor.submit(() -> checkoutRace(start, otherShowtimeId, second)));
			start.countDown();
			List<Object> results = new ArrayList<>();
			for (Future<Object> attempt : attempts) {
				results.add(attempt.get());
			}

			assertThat(results).filteredOn(CheckoutResult.class::isInstance).hasSize(1);
			assertThat(results)
				.filteredOn(BookingException.class::isInstance)
				.singleElement()
				.extracting(result -> ((BookingException) result).code())
				.isEqualTo("booking.idempotency_conflict");
		}
		finally {
			executor.shutdownNow();
		}
		assertThat(paymentCount(showtimeId) + paymentCount(otherShowtimeId)).isOne();
	}

	private Object checkoutRace(CountDownLatch start, long showtimeId, CheckoutRequest request) {
		try {
			start.await();
			return booking.checkout(showtimeId, request);
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return exception;
		}
		catch (RuntimeException exception) {
			return exception;
		}
	}

	@Test
	void aReusedIdempotencyKeyWithADifferentRequestIsAConflict() throws Exception {
		clock.set(START);
		int hallId = createHall("Fingerprint " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Idempotency Fingerprint", 90));
		int[] seats = seatIds(hallId);
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seats[0]);
		String idempotencyKey = UUID.randomUUID().toString();

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, idempotencyKey,
				ticket(seats[0], "ADULT"))
			.andExpect(status().isCreated());

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, idempotencyKey,
				ticket(seats[0], "CHILD"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.idempotency_conflict"));

		String otherHoldId = createHold(showtimeId, seats[1]);
		checkout(showtimeId, otherHoldId, "aisyah@example.com", SUCCESS_CARD, idempotencyKey,
				ticket(seats[1], "ADULT"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.idempotency_conflict"));

		assertThat(bookingCount(showtimeId)).isOne();
		assertThat(paymentCount(showtimeId)).isOne();
	}

	@Test
	void aReplayToADifferentShowtimeUrlIsAConflict() throws Exception {
		clock.set(START);
		int hallId = createHall("Scope " + UUID.randomUUID(), 1, 2);
		long movieId = insertMovie("Scoped Replay", 90);
		long showtimeId = insertShowtime(hallId, movieId);
		int otherHallId = createHall("Scope other " + UUID.randomUUID(), 1, 2);
		long otherShowtimeId = insertShowtime(otherHallId, movieId);
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String holdId = createHold(showtimeId, seatId);
		String idempotencyKey = UUID.randomUUID().toString();

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, idempotencyKey, ticket(seatId, "ADULT"))
			.andExpect(status().isCreated());

		checkout(otherShowtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, idempotencyKey, ticket(seatId, "ADULT"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.idempotency_conflict"));

		assertThat(bookingCount(showtimeId)).isOne();
		assertThat(bookingCount(otherShowtimeId)).isZero();
		assertThat(paymentCount(showtimeId)).isOne();
	}

	@Test
	void aLargeBookingTotalIsRecordedExactly() throws Exception {
		clock.set(START);
		int hallId = createHall("Large " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Large Total", 90));
		int[] seats = seatIds(hallId);
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		jdbcTemplate.update("update cineflow.showtimes set adult_price_myr = 999999.99 where id = ?", showtimeId);
		String holdId = createHold(showtimeId, seats);

		checkout(showtimeId, holdId, "aisyah@example.com", SUCCESS_CARD, UUID.randomUUID().toString(),
				ticket(seats[0], "ADULT") + "," + ticket(seats[1], "ADULT"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.totalMyr").value(1999999.98));

		var payment = jdbcTemplate.queryForMap(
				"""
						select p.amount_myr
						from cineflow.payments p
						join cineflow.bookings b on b.id = p.booking_id
						where b.showtime_id = ?
						""",
				showtimeId);
		assertThat(payment.get("amount_myr").toString()).isEqualTo("1999999.98");
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

	private String createHold(long showtimeId, int... seatIds) throws Exception {
		StringBuilder selection = new StringBuilder();
		for (int index = 0; index < seatIds.length; index++) {
			if (index > 0) {
				selection.append(',');
			}
			selection.append(seatIds[index]);
		}
		MvcResult result = mockMvc.perform(post("/api/showtimes/" + showtimeId + "/holds")
					.contentType(MediaType.APPLICATION_JSON)
					.header("X-Forwarded-For", "hold-" + UUID.randomUUID())
					.content("{\"seatIds\":[" + selection + "]}"))
			.andExpect(status().isCreated())
			.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.holdId");
	}

	private int createHall(String name, int rows, int seatsPerRow) {
		return jdbcTemplate.queryForObject(
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
	}

	private int[] seatIds(int hallId) {
		jdbcTemplate.update(
				"""
						insert into cineflow.seats (hall_id, row_label, seat_number, disabled)
						values (?, 'A', 1, false), (?, 'A', 2, false)
						""",
				hallId,
				hallId);
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

	private long insertShowtime(int hallId, long movieId) {
		return jdbcTemplate.queryForObject(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
						values (?, ?, '2099-06-20T11:30:00Z', 28.00, 18.00)
						returning id
						""",
				Long.class,
				hallId,
				movieId);
	}

	private static String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
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
