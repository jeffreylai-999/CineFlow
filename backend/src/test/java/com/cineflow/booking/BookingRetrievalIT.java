package com.cineflow.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@Import({ TestcontainersConfiguration.class, BookingRetrievalIT.ClockConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
class BookingRetrievalIT {

	private static final Instant START = Instant.parse("2026-09-16T00:00:00Z");
	private static final String SUCCESS_CARD = "4242424242424242";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MutableClock clock;

	@Test
	void aMatchingEmailAndBookingReferenceRetrievesTheTicket() throws Exception {
		clock.set(START);
		long showtimeId = prepareShowtime("Retrieval Match");
		ConfirmedBooking booking = checkout(showtimeId, "Aisyah@Example.com");

		retrieve("AISYAH@example.com", booking.reference(), "test-" + UUID.randomUUID())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.bookingReference").value(booking.reference()))
			.andExpect(jsonPath("$.showtimeId").value(showtimeId))
			.andExpect(jsonPath("$.movieTitle").value("Retrieval Match"))
			.andExpect(jsonPath("$.hallName").value("Hall Retrieval Match"))
			.andExpect(jsonPath("$.startsAtCinemaTime").value("2099-06-20T19:30:00"))
			.andExpect(jsonPath("$.timeZone").value("Asia/Kuala_Lumpur"))
			.andExpect(jsonPath("$.email").value("aisyah@example.com"))
			.andExpect(jsonPath("$.seats[0].label").value("A1"))
			.andExpect(jsonPath("$.seats[0].ticketType").value("ADULT"))
			.andExpect(jsonPath("$.seats[0].priceMyr").value(28.00))
			.andExpect(jsonPath("$.totalMyr").value(28.00))
			.andExpect(jsonPath("$.admissionToken").doesNotExist());
	}

	@Test
	void anUnknownBookingReferenceAndAMismatchedEmailReturnOneSafeResponse() throws Exception {
		clock.set(START);
		long showtimeId = prepareShowtime("Safe Response");
		ConfirmedBooking booking = checkout(showtimeId, "aisyah@example.com");

		MvcResult unknownReference = retrieve("aisyah@example.com", "ZZZZZZZZZZ", "test-" + UUID.randomUUID())
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("booking.retrieval_failed"))
			.andReturn();
		MvcResult mismatchedEmail = retrieve("other@example.com", booking.reference(), "test-" + UUID.randomUUID())
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("booking.retrieval_failed"))
			.andReturn();

		String unknownBody = unknownReference.getResponse().getContentAsString().replaceAll(
				"\"correlationId\":\"[^\"]*\"", "");
		String mismatchedBody = mismatchedEmail.getResponse().getContentAsString().replaceAll(
				"\"correlationId\":\"[^\"]*\"", "");
		assertThat(unknownBody).isEqualTo(mismatchedBody);
		assertThat(unknownBody).doesNotContain("aisyah@example.com", "other@example.com", booking.reference());
	}

	@Test
	void anAnonymizedBookingIsNoLongerRetrievable() throws Exception {
		clock.set(START);
		long showtimeId = prepareShowtime("Anonymized Retrieval");
		ConfirmedBooking booking = checkout(showtimeId, "aisyah@example.com");

		retrieve("aisyah@example.com", booking.reference(), "test-" + UUID.randomUUID())
			.andExpect(status().isOk());

		jdbcTemplate.queryForObject(
				"select cineflow.anonymize_expired_booking_emails(?)",
				Integer.class,
				Timestamp.from(Instant.parse("2099-06-28T11:30:00Z")));

		retrieve("aisyah@example.com", booking.reference(), "test-" + UUID.randomUUID())
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("booking.retrieval_failed"));

		retrieve("anonymized@cineflow.invalid", booking.reference(), "test-" + UUID.randomUUID())
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("booking.retrieval_failed"));
	}

	@Test
	void retrievalIsRateLimitedPerClientAddress() throws Exception {
		clock.set(START);
		String address = "retrieval-" + UUID.randomUUID();

		for (int attempt = 0; attempt < 10; attempt++) {
			retrieve("aisyah@example.com", "ZZZZZZZZZZ", address)
				.andExpect(status().isNotFound());
		}

		retrieve("aisyah@example.com", "ZZZZZZZZZZ", address)
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("booking.rate_limited"));

		retrieve("aisyah@example.com", "ZZZZZZZZZZ", "retrieval-" + UUID.randomUUID())
			.andExpect(status().isNotFound());
	}

	@Test
	void retrievalLogsContainNoEmailOrAdmissionToken() throws Exception {
		clock.set(START);
		long showtimeId = prepareShowtime("Log Hygiene");
		ConfirmedBooking booking = checkout(showtimeId, "log-hygiene@example.com");
		Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		ListAppender<ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
		try {
			retrieve("log-hygiene@example.com", booking.reference(), "test-" + UUID.randomUUID())
				.andExpect(status().isOk());

			List<String> messages = appender.list.stream()
				.map(ILoggingEvent::getFormattedMessage)
				.toList();
			assertThat(messages).noneMatch(message -> message.contains("log-hygiene@example.com"));
			assertThat(messages).noneMatch(message -> message.contains(booking.admissionToken()));
		}
		finally {
			logger.detachAppender(appender);
		}
	}

	@Test
	void aMalformedRetrievalRequestIsRejected() throws Exception {
		clock.set(START);
		mockMvc.perform(post("/api/bookings/retrieve")
					.contentType(MediaType.APPLICATION_JSON)
					.header("X-Forwarded-For", "test-" + UUID.randomUUID())
					.content("{\"email\": \"not-an-email\", \"bookingReference\": \"\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("request.invalid"));
	}

	private long prepareShowtime(String movieTitle) {
		int hallId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.halls (name, row_count, seats_per_row, seat_map_locked, created_at)
						values (?, 1, 1, false, ?)
						returning id
						""",
				Integer.class,
				"Hall " + movieTitle,
				Timestamp.from(START));
		jdbcTemplate.update(
				"""
						insert into cineflow.seats (hall_id, row_label, seat_number, disabled)
						values (?, 'A', 1, false)
						""",
				hallId);
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
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

	private ConfirmedBooking checkout(long showtimeId, String email) throws Exception {
		int seatId = jdbcTemplate.queryForObject(
				"""
						select s.id from cineflow.seats s
						join cineflow.showtimes st on st.hall_id = s.hall_id
						where st.id = ?
						""",
				Integer.class,
				showtimeId);
		MvcResult hold = mockMvc.perform(post("/api/showtimes/" + showtimeId + "/holds")
					.contentType(MediaType.APPLICATION_JSON)
					.header("X-Forwarded-For", "hold-" + UUID.randomUUID())
					.content("{\"seatIds\":[" + seatId + "]}"))
			.andExpect(status().isCreated())
			.andReturn();
		String holdId = JsonPath.read(hold.getResponse().getContentAsString(), "$.holdId");

		MvcResult confirmation = mockMvc.perform(post("/api/showtimes/" + showtimeId + "/checkout")
					.contentType(MediaType.APPLICATION_JSON)
					.header("X-Forwarded-For", "checkout-" + UUID.randomUUID())
					.content("""
							{
							  "holdId": "%s",
							  "email": "%s",
							  "tickets": [{"seatId": %d, "ticketType": "ADULT"}],
							  "cardNumber": "%s",
							  "idempotencyKey": "%s"
							}
							""".formatted(holdId, email, seatId, SUCCESS_CARD, UUID.randomUUID())))
			.andExpect(status().isCreated())
			.andReturn();
		String body = confirmation.getResponse().getContentAsString();
		return new ConfirmedBooking(
				JsonPath.read(body, "$.bookingReference"),
				JsonPath.read(body, "$.admissionToken"));
	}

	private record ConfirmedBooking(String reference, String admissionToken) {
	}

	private ResultActions retrieve(
			String email,
			String bookingReference,
			String clientAddress) throws Exception {
		return mockMvc.perform(post("/api/bookings/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.header("X-Forwarded-For", clientAddress)
				.content("""
						{
						  "email": "%s",
						  "bookingReference": "%s"
						}
						""".formatted(email, bookingReference)));
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
