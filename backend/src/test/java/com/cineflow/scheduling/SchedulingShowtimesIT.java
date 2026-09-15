package com.cineflow.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.cineflow.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SchedulingShowtimesIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	private String cachedAdminToken;

	@Test
	void administratorSchedulesFromANonCinemaTimeClientUsingThePayloadZone() throws Exception {
		TimeZone original = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		try {
			String token = adminToken();
			int hallId = createHall(token, "Zone " + UUID.randomUUID());
			long movieId = insertMovie("Courier Gate 90", 90);

			String created = createShowtime(token, movieId, hallId, "2026-09-20T19:30", "28.00", "18.00")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.startsAt").value("2026-09-20T11:30:00Z"))
				.andExpect(jsonPath("$.startsAtCinemaTime").value("2026-09-20T19:30:00"))
				.andExpect(jsonPath("$.timeZone").value("Asia/Kuala_Lumpur"))
				.andExpect(jsonPath("$.occupancyEndsAt").value("2026-09-20T13:15:00Z"))
				.andExpect(jsonPath("$.adultPriceMyr").value(28.00))
				.andExpect(jsonPath("$.childPriceMyr").value(18.00))
				.andReturn()
				.getResponse()
				.getContentAsString();
			int showtimeId = JsonPath.read(created, "$.id");

			createShowtime(token, movieId, hallId, "2026-09-20T21:00", "28.00", "18.00")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("scheduling.showtime_overlap"));

			createShowtime(token, movieId, hallId, "2026-09-20T21:15", "28.00", "18.00")
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.startsAt").value("2026-09-20T13:15:00Z"));

			assertThat(jdbcTemplate.queryForObject(
					"select starts_at from cineflow.showtimes where id = ?",
					java.time.OffsetDateTime.class,
					showtimeId)
				.toInstant()).isEqualTo(java.time.Instant.parse("2026-09-20T11:30:00Z"));
			assertThat(auditActions()).contains("SHOWTIME_CREATED");
		}
		finally {
			TimeZone.setDefault(original);
		}
	}

	@Test
	void databaseExclusionProtectsOverlapUnderConcurrentRequests() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Race " + UUID.randomUUID());
		long movieId = insertMovie("Concurrent Gate", 90);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger created = new AtomicInteger();
		AtomicInteger conflicts = new AtomicInteger();
		var pool = Executors.newFixedThreadPool(2);
		try {
			Future<?> first = pool.submit(() -> postOverlapping(token, movieId, hallId, "2026-10-01T19:30", ready, start, created, conflicts));
			Future<?> second = pool.submit(() -> postOverlapping(token, movieId, hallId, "2026-10-01T20:00", ready, start, created, conflicts));
			assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
			start.countDown();
			first.get(10, TimeUnit.SECONDS);
			second.get(10, TimeUnit.SECONDS);
		}
		finally {
			pool.shutdownNow();
		}
		assertThat(created.get()).isEqualTo(1);
		assertThat(conflicts.get()).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from cineflow.showtimes where hall_id = ?",
				Integer.class,
				hallId)).isEqualTo(1);
	}

	@Test
	void unusedFutureShowtimeCanBeRemovedWhileBookedOnesStay() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Remove " + UUID.randomUUID());
		int seatId = firstSeatId(hallId);
		long movieId = insertMovie("Removable Gate", 90);

		String created = createShowtime(token, movieId, hallId, "2026-11-01T19:30", "22.00", "12.00")
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		int removableId = JsonPath.read(created, "$.id");

		String booked = createShowtime(token, movieId, hallId, "2026-11-02T19:30", "22.00", "12.00")
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		int bookedId = JsonPath.read(booked, "$.id");
		insertBooking(bookedId, hallId, seatId);

		mockMvc.perform(delete("/api/showtimes/" + removableId).header("Authorization", "Bearer " + token))
			.andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/showtimes/" + bookedId).header("Authorization", "Bearer " + token))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("scheduling.showtime_has_bookings"));

		long pastId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
						values (?, ?, now() - interval '1 day', 22.00, 12.00)
						returning id
						""",
				Long.class,
				hallId,
				movieId);
		mockMvc.perform(delete("/api/showtimes/" + pastId).header("Authorization", "Bearer " + token))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("scheduling.showtime_not_removable"));

		assertThat(auditActions()).contains("SHOWTIME_REMOVED");
	}

	@Test
	void archivedMoviesAndHallsCannotReceiveNewShowtimes() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Archive hall " + UUID.randomUUID());
		long movieId = insertMovie("Archive movie", 90);
		jdbcTemplate.update("update cineflow.movies set archived_at = now() where id = ?", movieId);

		createShowtime(token, movieId, hallId, "2026-12-01T19:30", "28.00", "18.00")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("scheduling.movie_archived"));

		long activeMovie = insertMovie("Active movie", 90);
		mockMvc.perform(post("/api/halls/" + hallId + "/archive").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk());
		createShowtime(token, activeMovie, hallId, "2026-12-01T19:30", "28.00", "18.00")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("scheduling.hall_archived"));

		assertThatThrownBy(() -> jdbcTemplate.update(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
						values (?, ?, now() + interval '3 days', 28.00, 18.00)
						""",
				hallId,
				activeMovie))
			.hasMessageContaining("scheduling.hall_archived");
	}

	@Test
	void ticketPricesCanChangeAfterTheMovieAndHallAreArchived() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Priced archive " + UUID.randomUUID());
		long movieId = insertMovie("Archived priced", 90);
		String created = createShowtime(token, movieId, hallId, "2026-12-16T18:00", "28.00", "18.00")
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		int showtimeId = JsonPath.read(created, "$.id");

		jdbcTemplate.update("update cineflow.movies set archived_at = now() where id = ?", movieId);
		mockMvc.perform(post("/api/halls/" + hallId + "/archive").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk());

		mockMvc.perform(patch("/api/showtimes/" + showtimeId)
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"adultPriceMyr":31.00,"childPriceMyr":15.50}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.adultPriceMyr").value(31.00))
			.andExpect(jsonPath("$.childPriceMyr").value(15.50));
	}

	@Test
	void pricingChangesAreAudited() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Price " + UUID.randomUUID());
		long movieId = insertMovie("Priced Gate", 90);
		String created = createShowtime(token, movieId, hallId, "2026-12-15T18:00", "28.00", "18.00")
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		int showtimeId = JsonPath.read(created, "$.id");

		mockMvc.perform(patch("/api/showtimes/" + showtimeId)
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"adultPriceMyr":30.50,"childPriceMyr":16.00}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.adultPriceMyr").value(30.50))
			.andExpect(jsonPath("$.childPriceMyr").value(16.00))
			.andExpect(jsonPath("$.startsAtCinemaTime").value("2026-12-15T18:00:00"));

		mockMvc.perform(get("/api/showtimes").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.id==%d)].adultPriceMyr", showtimeId).value(org.hamcrest.Matchers.hasItem(30.50)));
		assertThat(auditActions()).contains("SHOWTIME_CREATED", "SHOWTIME_PRICES_UPDATED");
	}

	@Test
	void bookingStaffCannotScheduleShowtimes() throws Exception {
		String staffToken = accessToken("booking.staff", "StaffPassw0rd!");
		mockMvc.perform(get("/api/showtimes").header("Authorization", "Bearer " + staffToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));
		createShowtime(staffToken, 1, 1, "2026-12-20T19:30", "28.00", "18.00")
			.andExpect(status().isForbidden());
	}

	private void postOverlapping(
			String token,
			long movieId,
			int hallId,
			String local,
			CountDownLatch ready,
			CountDownLatch start,
			AtomicInteger created,
			AtomicInteger conflicts) {
		try {
			ready.countDown();
			assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
			MvcResult result = createShowtime(token, movieId, hallId, local, "28.00", "18.00").andReturn();
			int status = result.getResponse().getStatus();
			if (status == 201) {
				created.incrementAndGet();
			}
			else if (status == 409) {
				conflicts.incrementAndGet();
			}
			else {
				throw new IllegalStateException("unexpected status " + status + " " + result.getResponse().getContentAsString());
			}
		}
		catch (Exception exception) {
			throw new IllegalStateException(exception);
		}
	}

	private ResultActions createShowtime(
			String token,
			long movieId,
			int hallId,
			String startsAtLocal,
			String adult,
			String child) throws Exception {
		return mockMvc.perform(post("/api/showtimes")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "movieId":%d,
						  "hallId":%d,
						  "startsAtLocal":"%s",
						  "timeZone":"Asia/Kuala_Lumpur",
						  "adultPriceMyr":%s,
						  "childPriceMyr":%s
						}
						""".formatted(movieId, hallId, startsAtLocal, adult, child)));
	}

	private int createHall(String token, String name) throws Exception {
		MvcResult created = mockMvc.perform(post("/api/halls")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"%s","rowCount":1,"seatsPerRow":2}
						""".formatted(name)))
			.andExpect(status().isCreated())
			.andReturn();
		return JsonPath.read(created.getResponse().getContentAsString(), "$.id");
	}

	private int firstSeatId(int hallId) {
		return jdbcTemplate.queryForObject(
				"select id from cineflow.seats where hall_id = ? order by id limit 1",
				Integer.class,
				hallId);
	}

	private long insertMovie(String title, int runtimeMinutes) {
		return jdbcTemplate.queryForObject(
				"""
						insert into cineflow.movies (
						    title, synopsis, genre, runtime_minutes, age_rating, poster_url,
						    source_provider, external_id, source_refreshed_at)
						values (?, 'Synopsis', 'Adventure', ?, 'PG', null, 'fixture', ?, now())
						returning id
						""",
				Long.class,
				title,
				runtimeMinutes,
				UUID.randomUUID().toString());
	}

	private void insertBooking(long showtimeId, int hallId, int seatId) {
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (showtime_id, hall_id, seat_id, claim_kind, expires_at)
						values (?, ?, ?, 'BOOKING', null)
						""",
				showtimeId,
				hallId,
				seatId);
	}

	private String adminToken() throws Exception {
		return accessToken("administrator", "AdminPassw0rd!");
	}

	private String accessToken(String username, String password) throws Exception {
		if ("administrator".equals(username) && cachedAdminToken != null) {
			return cachedAdminToken;
		}
		MvcResult login = mockMvc.perform(post("/api/auth/login")
				.header("X-Forwarded-For", "198.51.100.81")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"%s"}
						""".formatted(username, password)))
			.andExpect(status().isOk())
			.andReturn();
		String token = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
		if ("administrator".equals(username)) {
			cachedAdminToken = token;
		}
		return token;
	}

	private List<String> auditActions() {
		return jdbcTemplate.queryForList("select action from cineflow.audit_events", String.class);
	}
}
