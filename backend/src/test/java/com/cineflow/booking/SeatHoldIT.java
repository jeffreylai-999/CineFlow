package com.cineflow.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.jayway.jsonpath.JsonPath;

@Import({ TestcontainersConfiguration.class, SeatHoldIT.ClockConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
class SeatHoldIT {

	private static final Instant START = Instant.parse("2026-09-16T00:00:00Z");

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MutableClock clock;

	@Autowired
	Booking booking;

	@Autowired
	SeatHoldExpiryService seatHoldExpiryService;

	@Test
	void onlineCustomerCanHoldSelectedSeatsForTenMinutes() throws Exception {
		clock.set(START);
		int hallId = createHall("Hold " + UUID.randomUUID(), 1, 2);
		long movieId = insertMovie("Seat Hold Gate", 90);
		long showtimeId = insertShowtime(hallId, movieId);
		int[] seats = seatIds(hallId);
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);

		mockMvc.perform(post("/api/showtimes/" + showtimeId + "/holds")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"seatIds":[%d,%d]}
						""".formatted(seats[1], seats[0])))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.holdId").isString())
			.andExpect(jsonPath("$.showtimeId").value(showtimeId))
			.andExpect(jsonPath("$.seatIds[0]").value(seats[0]))
			.andExpect(jsonPath("$.seatIds[1]").value(seats[1]))
			.andExpect(jsonPath("$.expiresAt").value("2026-09-16T00:10:00Z"));
	}

	@Test
	void competingSeatHoldReturnsAStableConflict() throws Exception {
		clock.set(START);
		int hallId = createHall("Conflict " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Seat Hold Conflict", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);

		createHold(showtimeId, seatId)
			.andExpect(status().isCreated());

		createHold(showtimeId, seatId)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.seats_unavailable"))
			.andExpect(jsonPath("$.detail").doesNotExist());
	}

	@Test
	void anExpiredHoldIsReleasedBeforeTheNextAcquisition() throws Exception {
		clock.set(START);
		int hallId = createHall("Expired " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Expired Seat Hold", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);

		createHold(showtimeId, seatId)
			.andExpect(status().isCreated());

		clock.set(START.plusSeconds(600));
		createHold(showtimeId, seatId)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.expiresAt").value("2026-09-16T00:20:00Z"));

		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from cineflow.seat_claims where showtime_id = ? and seat_id = ?",
				Integer.class,
				showtimeId,
				seatId)).isEqualTo(1);
	}

	@Test
	void expiryCleanupReturnsSeatsToCustomerAvailabilityAndRemovesExpiredHoldRecords() throws Exception {
		clock.set(START);
		int hallId = createHall("Expiry cleanup " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Expired Seat Hold Cleanup", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		createHold(showtimeId, seatId)
			.andExpect(status().isCreated());

		clock.set(START.plusSeconds(600));
		seatHoldExpiryService.releaseExpiredHolds();

		mockMvc.perform(get("/api/showtimes/" + showtimeId + "/seats"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.seats[0].available").value(true));
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from cineflow.seat_claims where showtime_id = ?",
				Integer.class,
				showtimeId)).isZero();
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from cineflow.seat_holds where showtime_id = ?",
				Integer.class,
				showtimeId)).isZero();
	}

	@Test
	void concurrentAcquisitionAllowsOnlyOneHoldForTheSameSeat() throws Exception {
		clock.set(START);
		int hallId = createHall("Concurrent " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Concurrent Seat Hold", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			List<Future<Throwable>> attempts = List.of(
					executor.submit(() -> createConcurrentHold(start, showtimeId, seatId)),
					executor.submit(() -> createConcurrentHold(start, showtimeId, seatId)));
			start.countDown();
			List<Throwable> results = new ArrayList<>();
			for (Future<Throwable> attempt : attempts) {
				results.add(attempt.get());
			}

			assertThat(results).filteredOn(result -> result == null).hasSize(1);
			assertThat(results).filteredOn(result -> result != null)
				.singleElement()
				.isInstanceOf(BookingException.class)
				.extracting(result -> ((BookingException) result).code())
				.isEqualTo("booking.seats_unavailable");
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test
	void seatHoldAcquisitionIsRateLimitedPerClientAddress() throws Exception {
		clock.set(START);
		int hallId = createHall("Rate limit " + UUID.randomUUID(), 1, 2);
		long showtimeId = insertShowtime(hallId, insertMovie("Rate Limited Hold", 90));
		int seatId = seatIds(hallId)[0];
		jdbcTemplate.update("update cineflow.halls set seat_map_locked = true where id = ?", hallId);
		String address = "rate-limit-" + UUID.randomUUID();

		createHold(showtimeId, seatId, address)
			.andExpect(status().isCreated());
		for (int attempt = 1; attempt < 10; attempt++) {
			createHold(showtimeId, seatId, address)
				.andExpect(status().isConflict());
		}

		createHold(showtimeId, seatId, address)
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("booking.rate_limited"));
	}

	private Throwable createConcurrentHold(CountDownLatch start, long showtimeId, int seatId) {
		try {
			start.await();
			booking.createSeatHold(showtimeId, List.of((long) seatId));
			return null;
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return exception;
		}
		catch (RuntimeException exception) {
			return exception;
		}
	}

	private ResultActions createHold(long showtimeId, int seatId) throws Exception {
		return createHold(showtimeId, seatId, null);
	}

	private ResultActions createHold(long showtimeId, int seatId, String clientAddress) throws Exception {
		var request = post("/api/showtimes/" + showtimeId + "/holds")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"seatIds":[%d]}
						""".formatted(seatId));
		if (clientAddress != null) {
			request.header("X-Forwarded-For", clientAddress);
		}
		return mockMvc.perform(request);
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

	@TestConfiguration
	static class ClockConfig {

		@Bean
		@Primary
		MutableClock mutableClock() {
			return new MutableClock(START);
		}
	}
}
