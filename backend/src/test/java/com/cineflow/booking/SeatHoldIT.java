package com.cineflow.booking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

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
