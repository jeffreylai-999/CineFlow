package com.cineflow.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.hamcrest.Matchers;
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
class BookingCustomerIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	private String cachedAdminToken;

	@Test
	void publicCatalogListsOnlyMoviesWithCurrentOrFutureShowtimesGroupedByCinemaDate() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Browse " + UUID.randomUUID());
		long listedMovieId = insertMovie("Listed Gate", 90);
		insertMovie("Hidden Gate", 90);
		createShowtime(token, listedMovieId, hallId, "2099-06-01T14:00", "26.00", "16.00")
			.andExpect(status().isCreated());
		createShowtime(token, listedMovieId, hallId, "2099-06-01T19:30", "28.00", "18.00")
			.andExpect(status().isCreated());
		createShowtime(token, listedMovieId, hallId, "2099-06-02T19:30", "30.00", "20.00")
			.andExpect(status().isCreated());

		mockMvc.perform(get("/api/movies").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].title", Matchers.not(Matchers.hasItem("Hidden Gate"))))
			.andExpect(jsonPath("$[?(@.title=='Listed Gate')].dates[0].cinemaDate").value(Matchers.hasItem("2099-06-01")))
			.andExpect(jsonPath("$[?(@.title=='Listed Gate')].dates[0].showtimes.length()").value(Matchers.hasItem(2)))
			.andExpect(jsonPath("$[?(@.title=='Listed Gate')].dates[0].showtimes[0].startsAtCinemaTime")
				.value(Matchers.hasItem("2099-06-01T14:00:00")))
			.andExpect(jsonPath("$[?(@.title=='Listed Gate')].dates[0].showtimes[0].adultPriceMyr")
				.value(Matchers.hasItem(26.00)))
			.andExpect(jsonPath("$[?(@.title=='Listed Gate')].dates[0].showtimes[0].childPriceMyr")
				.value(Matchers.hasItem(16.00)))
			.andExpect(jsonPath("$[?(@.title=='Listed Gate')].dates[0].showtimes[0].checkoutOpen")
				.value(Matchers.hasItem(true)))
			.andExpect(jsonPath("$[?(@.title=='Listed Gate')].dates[1].cinemaDate").value(Matchers.hasItem("2099-06-02")));
	}

	@Test
	void customerSeatMapCollapsesHoldBookingAndDisabledReasons() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Seats " + UUID.randomUUID(), 1, 4);
		int[] seatIds = seatIds(hallId);
		long movieId = insertMovie("Availability Gate", 90);
		String created = createShowtime(token, movieId, hallId, "2099-06-10T19:30", "28.00", "18.00")
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		int showtimeId = JsonPath.read(created, "$.id");

		jdbcTemplate.update("update cineflow.seats set disabled = true where id = ?", seatIds[1]);
		insertHold(showtimeId, hallId, seatIds[2], "now() + interval '10 minutes'");
		insertBooking(showtimeId, hallId, seatIds[3]);

		MvcResult result = mockMvc.perform(get("/api/showtimes/" + showtimeId + "/seats").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.showtimeId").value(showtimeId))
			.andExpect(jsonPath("$.movieTitle").value("Availability Gate"))
			.andExpect(jsonPath("$.bookingLimit").value(10))
			.andExpect(jsonPath("$.adultPriceMyr").value(28.00))
			.andExpect(jsonPath("$.childPriceMyr").value(18.00))
			.andExpect(jsonPath("$.seats.length()").value(4))
			.andExpect(jsonPath("$.seats[0].available").value(true))
			.andExpect(jsonPath("$.seats[1].available").value(false))
			.andExpect(jsonPath("$.seats[2].available").value(false))
			.andExpect(jsonPath("$.seats[3].available").value(false))
			.andExpect(jsonPath("$.seats[0].label").value("A1"))
			.andReturn();

		String body = result.getResponse().getContentAsString();
		assertThat(body).doesNotContain("disabled", "HOLD", "BOOKING", "Seat Hold", "claimKind", "claim_kind");
	}

	@Test
	void expiredSeatHoldIsAvailableOnTheCustomerMap() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Expired " + UUID.randomUUID(), 1, 2);
		int heldSeat = seatIds(hallId)[0];
		long movieId = insertMovie("Expired Hold Browse", 90);
		String created = createShowtime(token, movieId, hallId, "2099-06-11T19:30", "22.00", "12.00")
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		int showtimeId = JsonPath.read(created, "$.id");
		insertHold(showtimeId, hallId, heldSeat, "now() - interval '1 minute'");

		mockMvc.perform(get("/api/showtimes/" + showtimeId + "/seats"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.seats[0].available").value(true));
	}

	@Test
	void onlineCheckoutCannotBeginAtOrAfterTheBookingCutoff() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Cutoff " + UUID.randomUUID(), 1, 2);
		long movieId = insertMovie("Cutoff Gate", 90);
		long showtimeId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
						values (?, ?, now() + interval '10 minutes', 24.00, 14.00)
						returning id
						""",
				Long.class,
				hallId,
				movieId);

		mockMvc.perform(get("/api/movies").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.title=='Cutoff Gate')].dates[0].showtimes[0].checkoutOpen")
				.value(Matchers.hasItem(false)));

		mockMvc.perform(get("/api/showtimes/" + showtimeId + "/seats").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("booking.cutoff"))
			.andExpect(jsonPath("$.title").value("Online checkout is closed at the Booking Cutoff"))
			.andExpect(jsonPath("$.detail").doesNotExist());
	}

	@Test
	void aCurrentShowtimeStaysOnTheCatalogAfterCheckoutCloses() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Current " + UUID.randomUUID(), 1, 2);
		long movieId = insertMovie("Current Gate", 90);
		jdbcTemplate.update(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
						values (?, ?, now() - interval '20 minutes', 24.00, 14.00)
						""",
				hallId,
				movieId);

		mockMvc.perform(get("/api/movies").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.title=='Current Gate')]").exists())
			.andExpect(jsonPath("$[?(@.title=='Current Gate')].dates[0].showtimes[0].checkoutOpen")
				.value(Matchers.hasItem(false)));
	}

	@Test
	void aFinishedShowtimeDoesNotKeepItsMovieInTheCatalog() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Past " + UUID.randomUUID(), 1, 2);
		long movieId = insertMovie("Finished Gate", 90);
		jdbcTemplate.update(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
						values (?, ?, now() - interval '3 hours', 24.00, 14.00)
						""",
				hallId,
				movieId);

		mockMvc.perform(get("/api/movies").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].title", Matchers.not(Matchers.hasItem("Finished Gate"))));
	}

	@Test
	void unknownShowtimeSeatMapIsNotFound() throws Exception {
		mockMvc.perform(get("/api/showtimes/999999/seats").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("booking.showtime_not_found"));
	}

	@Test
	void archivedMovieShowtimeSeatMapIsNotFound() throws Exception {
		String token = adminToken();
		int hallId = createHall(token, "Archived " + UUID.randomUUID(), 1, 2);
		long movieId = insertMovie("Archived Gate", 90);
		String created = createShowtime(token, movieId, hallId, "2099-06-12T19:30", "22.00", "12.00")
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		int showtimeId = JsonPath.read(created, "$.id");
		jdbcTemplate.update("update cineflow.movies set archived_at = now() where id = ?", movieId);

		mockMvc.perform(get("/api/movies").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].title", Matchers.not(Matchers.hasItem("Archived Gate"))));

		mockMvc.perform(get("/api/showtimes/" + showtimeId + "/seats").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("booking.showtime_not_found"));
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
		return createHall(token, name, 1, 2);
	}

	private int createHall(String token, String name, int rowCount, int seatsPerRow) throws Exception {
		MvcResult created = mockMvc.perform(post("/api/halls")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"%s","rowCount":%d,"seatsPerRow":%d}
						""".formatted(name, rowCount, seatsPerRow)))
			.andExpect(status().isCreated())
			.andReturn();
		return JsonPath.read(created.getResponse().getContentAsString(), "$.id");
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
						values (?, 'Synopsis', 'Adventure', ?, 'PG', null, 'fixture', ?, now())
						returning id
						""",
				Long.class,
				title,
				runtimeMinutes,
				UUID.randomUUID().toString());
	}

	private void insertHold(long showtimeId, int hallId, int seatId, String expiresAtSql) {
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (showtime_id, hall_id, seat_id, claim_kind, expires_at)
						values (?, ?, ?, 'HOLD', %s)
						""".formatted(expiresAtSql),
				showtimeId,
				hallId,
				seatId);
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
		if (cachedAdminToken != null) {
			return cachedAdminToken;
		}
		MvcResult login = mockMvc.perform(post("/api/auth/login")
				.header("X-Forwarded-For", "198.51.100.91")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"administrator","password":"AdminPassw0rd!"}
						"""))
			.andExpect(status().isOk())
			.andReturn();
		cachedAdminToken = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
		return cachedAdminToken;
	}

}
