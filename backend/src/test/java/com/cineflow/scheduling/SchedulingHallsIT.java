package com.cineflow.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
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
class SchedulingHallsIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void administratorCreatesAHallWithStableLabelledSeats() throws Exception {
		String adminToken = accessToken("administrator", "AdminPassw0rd!");
		String name = "Hall " + UUID.randomUUID();

		mockMvc.perform(post("/api/halls")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"%s","rowCount":2,"seatsPerRow":3}
						""".formatted(name)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value(name))
			.andExpect(jsonPath("$.rowCount").value(2))
			.andExpect(jsonPath("$.seatsPerRow").value(3))
			.andExpect(jsonPath("$.archivedAt").doesNotExist())
			.andExpect(jsonPath("$.seats.length()").value(6))
			.andExpect(jsonPath("$.seats[0].rowLabel").value("A"))
			.andExpect(jsonPath("$.seats[0].seatNumber").value(1))
			.andExpect(jsonPath("$.seats[0].label").value("A1"))
			.andExpect(jsonPath("$.seats[0].disabled").value(false))
			.andExpect(jsonPath("$.seats[2].label").value("A3"))
			.andExpect(jsonPath("$.seats[3].label").value("B1"))
			.andExpect(jsonPath("$.seats[5].label").value("B3"));
	}

	@Test
	void seatMapGeometryAndIdentitiesCannotChangeAfterCreation() throws Exception {
		String adminToken = accessToken("administrator", "AdminPassw0rd!");
		MvcResult created = createHall(adminToken, "Locked " + UUID.randomUUID(), 2, 3)
			.andExpect(status().isCreated())
			.andReturn();
		int hallId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
		int firstSeatId = JsonPath.read(created.getResponse().getContentAsString(), "$.seats[0].id");

		assertThatThrownBy(() -> jdbcTemplate.update(
				"update cineflow.seats set row_label = 'Z' where id = ?", firstSeatId))
			.hasMessageContaining("scheduling.seat_identity_immutable");
		assertThatThrownBy(() -> jdbcTemplate.update(
				"update cineflow.seats set seat_number = 9 where id = ?", firstSeatId))
			.hasMessageContaining("scheduling.seat_identity_immutable");
		assertThatThrownBy(() -> jdbcTemplate.update(
				"insert into cineflow.seats (hall_id, row_label, seat_number, disabled) values (?, 'C', 1, false)",
				hallId))
			.hasMessageContaining("scheduling.seat_map_locked");
		assertThatThrownBy(() -> jdbcTemplate.update("delete from cineflow.seats where id = ?", firstSeatId))
			.hasMessageContaining("scheduling.seat_map_locked");
		assertThatThrownBy(() -> jdbcTemplate.update(
				"update cineflow.halls set row_count = 5 where id = ?", hallId))
			.hasMessageContaining("scheduling.seat_map_locked");

		mockMvc.perform(get("/api/halls/" + hallId).header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.rowCount").value(2))
			.andExpect(jsonPath("$.seatsPerRow").value(3))
			.andExpect(jsonPath("$.seats.length()").value(6))
			.andExpect(jsonPath("$.seats[0].id").value(firstSeatId))
			.andExpect(jsonPath("$.seats[0].label").value("A1"))
			.andExpect(jsonPath("$.seats[5].label").value("B3"));

		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from cineflow.seats where hall_id = ?", Integer.class, hallId))
			.isEqualTo(6);
	}

	@Test
	void administratorCanEnableAndDisableASeatWithoutClaims() throws Exception {
		String adminToken = accessToken("administrator", "AdminPassw0rd!");
		MvcResult created = createHall(adminToken, "Toggle " + UUID.randomUUID(), 1, 2)
			.andExpect(status().isCreated())
			.andReturn();
		int hallId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
		int seatId = JsonPath.read(created.getResponse().getContentAsString(), "$.seats[0].id");

		mockMvc.perform(patch("/api/halls/" + hallId + "/seats/" + seatId)
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"disabled\":true}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(seatId))
			.andExpect(jsonPath("$.label").value("A1"))
			.andExpect(jsonPath("$.disabled").value(true));

		mockMvc.perform(get("/api/halls/" + hallId).header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.seats[0].disabled").value(true))
			.andExpect(jsonPath("$.seats[1].disabled").value(false));

		mockMvc.perform(patch("/api/halls/" + hallId + "/seats/" + seatId)
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"disabled\":false}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.disabled").value(false));
	}

	@Test
	void unsafeDisableReturnsStableProblemDetails() throws Exception {
		String adminToken = accessToken("administrator", "AdminPassw0rd!");
		MvcResult created = createHall(adminToken, "Protected " + UUID.randomUUID(), 1, 2)
			.andExpect(status().isCreated())
			.andReturn();
		int hallId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
		int heldSeatId = JsonPath.read(created.getResponse().getContentAsString(), "$.seats[0].id");
		int bookedSeatId = JsonPath.read(created.getResponse().getContentAsString(), "$.seats[1].id");
		long movieId = jdbcTemplate.queryForObject(
				"select id from cineflow.movies where external_id = 'nebula-express'", Long.class);
		long showtimeId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at)
						values (?, ?, now() + interval '2 days')
						returning id
						""",
				Long.class,
				hallId,
				movieId);
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (showtime_id, seat_id, claim_kind, expires_at)
						values (?, ?, 'HOLD', now() + interval '10 minutes')
						""",
				showtimeId,
				heldSeatId);
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (showtime_id, seat_id, claim_kind, expires_at)
						values (?, ?, 'BOOKING', null)
						""",
				showtimeId,
				bookedSeatId);

		mockMvc.perform(patch("/api/halls/" + hallId + "/seats/" + heldSeatId)
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"disabled\":true}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("scheduling.seat_not_disableable"));
		mockMvc.perform(patch("/api/halls/" + hallId + "/seats/" + bookedSeatId)
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"disabled\":true}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("scheduling.seat_not_disableable"));

		assertThatThrownBy(() -> jdbcTemplate.update(
				"update cineflow.seats set disabled = true where id = ?", heldSeatId))
			.hasMessageContaining("scheduling.seat_not_disableable");

		mockMvc.perform(get("/api/halls/" + hallId).header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.seats[0].disabled").value(false))
			.andExpect(jsonPath("$.seats[1].disabled").value(false));
	}

	@Test
	void expiredHoldOrPastBookingDoesNotBlockDisable() throws Exception {
		String adminToken = accessToken("administrator", "AdminPassw0rd!");
		MvcResult created = createHall(adminToken, "Released " + UUID.randomUUID(), 1, 2)
			.andExpect(status().isCreated())
			.andReturn();
		int hallId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
		int heldSeatId = JsonPath.read(created.getResponse().getContentAsString(), "$.seats[0].id");
		int bookedSeatId = JsonPath.read(created.getResponse().getContentAsString(), "$.seats[1].id");
		long movieId = jdbcTemplate.queryForObject(
				"select id from cineflow.movies where external_id = 'nebula-express'", Long.class);
		long expiredHoldShowtime = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at)
						values (?, ?, now() + interval '2 days')
						returning id
						""",
				Long.class,
				hallId,
				movieId);
		long pastShowtime = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at)
						values (?, ?, now() - interval '1 day')
						returning id
						""",
				Long.class,
				hallId,
				movieId);
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (showtime_id, seat_id, claim_kind, expires_at)
						values (?, ?, 'HOLD', now() - interval '1 minute')
						""",
				expiredHoldShowtime,
				heldSeatId);
		jdbcTemplate.update(
				"""
						insert into cineflow.seat_claims (showtime_id, seat_id, claim_kind, expires_at)
						values (?, ?, 'BOOKING', null)
						""",
				pastShowtime,
				bookedSeatId);

		mockMvc.perform(patch("/api/halls/" + hallId + "/seats/" + heldSeatId)
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"disabled\":true}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.disabled").value(true));
		mockMvc.perform(patch("/api/halls/" + hallId + "/seats/" + bookedSeatId)
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"disabled\":true}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.disabled").value(true));
	}

	@Test
	void archivingAHallPreventsNewShowtimesAndRetainsSeats() throws Exception {
		String adminToken = accessToken("administrator", "AdminPassw0rd!");
		MvcResult created = createHall(adminToken, "Archive " + UUID.randomUUID(), 2, 2)
			.andExpect(status().isCreated())
			.andReturn();
		int hallId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");
		int firstSeatId = JsonPath.read(created.getResponse().getContentAsString(), "$.seats[0].id");
		long movieId = jdbcTemplate.queryForObject(
				"select id from cineflow.movies where external_id = 'nebula-express'", Long.class);

		mockMvc.perform(post("/api/halls/" + hallId + "/archive")
				.header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(hallId))
			.andExpect(jsonPath("$.archivedAt").isNotEmpty())
			.andExpect(jsonPath("$.seats.length()").value(4))
			.andExpect(jsonPath("$.seats[0].id").value(firstSeatId));

		assertThatThrownBy(() -> jdbcTemplate.update(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at)
						values (?, ?, now() + interval '1 day')
						""",
				hallId,
				movieId))
			.hasMessageContaining("scheduling.hall_archived");

		mockMvc.perform(get("/api/halls/" + hallId).header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.archivedAt").isNotEmpty())
			.andExpect(jsonPath("$.seats.length()").value(4))
			.andExpect(jsonPath("$.seats[0].label").value("A1"));

		MvcResult other = createHall(adminToken, "Active " + UUID.randomUUID(), 1, 1)
			.andExpect(status().isCreated())
			.andReturn();
		int activeHallId = JsonPath.read(other.getResponse().getContentAsString(), "$.id");
		long showtimeId = jdbcTemplate.queryForObject(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at)
						values (?, ?, now() + interval '1 day')
						returning id
						""",
				Long.class,
				activeHallId,
				movieId);
		assertThatThrownBy(() -> jdbcTemplate.update(
				"update cineflow.showtimes set hall_id = ? where id = ?", hallId, showtimeId))
			.hasMessageContaining("scheduling.hall_archived");
		assertThatThrownBy(() -> jdbcTemplate.update("delete from cineflow.halls where id = ?", hallId))
			.hasMessageContaining("violates foreign key constraint");
	}

	@Test
	void administratorCanListCreatedHalls() throws Exception {
		String adminToken = accessToken("administrator", "AdminPassw0rd!");
		String name = "Listed " + UUID.randomUUID();
		MvcResult created = createHall(adminToken, name, 1, 4).andExpect(status().isCreated()).andReturn();
		int hallId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(get("/api/halls").header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.id == %d)].name", hallId).value(name))
			.andExpect(jsonPath("$[?(@.id == %d)].rowCount", hallId).value(1))
			.andExpect(jsonPath("$[?(@.id == %d)].seatsPerRow", hallId).value(4));
	}

	@Test
	void bookingStaffCannotManageHalls() throws Exception {
		String staffToken = accessToken("booking.staff", "StaffPassw0rd!");
		String adminToken = accessToken("administrator", "AdminPassw0rd!");
		MvcResult created = createHall(adminToken, "Hidden " + UUID.randomUUID(), 1, 1)
			.andExpect(status().isCreated())
			.andReturn();
		int hallId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		createHall(staffToken, "Staff Hall", 1, 1)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));
		mockMvc.perform(get("/api/halls/" + hallId).header("Authorization", "Bearer " + staffToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));
		mockMvc.perform(post("/api/halls/" + hallId + "/archive")
				.header("Authorization", "Bearer " + staffToken))
			.andExpect(status().isForbidden());
	}

	private ResultActions createHall(String adminToken, String name, int rowCount, int seatsPerRow) throws Exception {
		return mockMvc.perform(post("/api/halls")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"%s","rowCount":%d,"seatsPerRow":%d}
						""".formatted(name, rowCount, seatsPerRow)));
	}

	private String accessToken(String username, String password) throws Exception {
		MvcResult login = login(username, password).andExpect(status().isOk()).andReturn();
		return JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
	}

	private ResultActions login(String username, String password) throws Exception {
		return mockMvc.perform(post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"%s","password":"%s"}
					""".formatted(username, password)));
	}
}
