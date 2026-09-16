package com.cineflow.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.cineflow.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SeatAvailabilityStompIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@LocalServerPort
	int port;

	private WebSocketStompClient stompClient;

	@BeforeEach
	void setUp() {
		stompClient = new WebSocketStompClient(new StandardWebSocketClient());
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	}

	@Test
	void committedSeatHoldPublishesShowtimeScopedAvailabilityInvalidation() throws Exception {
		long showtimeId = createShowtime();
		int seatId = jdbcTemplate.queryForObject(
				"select id from cineflow.seats where hall_id = (select hall_id from cineflow.showtimes where id = ?)",
				Integer.class,
				showtimeId);
		StompSession session = stompClient.connectAsync(
				"ws://localhost:" + port + "/ws",
				new StompSessionHandlerAdapter() {
				})
			.get(5, TimeUnit.SECONDS);
		CompletableFuture<Map<String, Object>> invalidation = new CompletableFuture<>();
		session.subscribe("/topic/showtimes/" + showtimeId + "/availability", frameHandler(invalidation));

		mockMvc.perform(post("/api/showtimes/" + showtimeId + "/holds")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"seatIds":[%d]}
						""".formatted(seatId)))
			.andExpect(status().isCreated());

		assertThat(invalidation.get(5, TimeUnit.SECONDS)).containsEntry("showtimeId", (int) showtimeId);
		session.disconnect();
	}

	private long createShowtime() {
		int hallId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.halls (name, row_count, seats_per_row, seat_map_locked, created_at)
						values (?, 1, 1, false, ?)
						returning id
						""",
				Integer.class,
				"Stomp " + UUID.randomUUID(),
				Timestamp.from(Instant.now()));
		jdbcTemplate.update(
				"insert into cineflow.seats (hall_id, row_label, seat_number, disabled) values (?, 'A', 1, false)",
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
				"Stomp Gate " + UUID.randomUUID(),
				UUID.randomUUID().toString(),
				Timestamp.from(Instant.now()));
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

	private static StompFrameHandler frameHandler(CompletableFuture<Map<String, Object>> invalidation) {
		return new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return Map.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				@SuppressWarnings("unchecked")
				Map<String, Object> body = (Map<String, Object>) payload;
				invalidation.complete(body);
			}
		};
	}
}
