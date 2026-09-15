package com.cineflow.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
class IdentityStaffAccountsIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void administratorCanCreateBookingStaffWithoutADefaultPassword() throws Exception {
		String username = uniqueUsername();
		String password = "UniquePassw0rd!";
		String adminToken = accessToken(login("administrator", "AdminPassw0rd!").andExpect(status().isOk()).andReturn());

		MvcResult created = mockMvc.perform(post("/api/staff/accounts")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"%s"}
						""".formatted(username, password)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.username").value(username))
			.andExpect(jsonPath("$.role").value("BOOKING_STAFF"))
			.andExpect(jsonPath("$.active").value(true))
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.passwordHash").doesNotExist())
			.andReturn();

		assertThat(created.getResponse().getContentAsString()).doesNotContain(password);
		String storedHash = jdbcTemplate.queryForObject(
				"select password_hash from cineflow.staff_accounts where username = ?",
				String.class,
				username);
		assertThat(storedHash).isNotEqualTo(password);
		assertThat(storedHash).startsWith("$2");

		login(username, password).andExpect(status().isOk());
		login(username, "AdminPassw0rd!").andExpect(status().isUnauthorized());
		login(username, "StaffPassw0rd!").andExpect(status().isUnauthorized());
	}

	@Test
	void administratorCanListStaffAccounts() throws Exception {
		String adminToken = accessToken(login("administrator", "AdminPassw0rd!").andExpect(status().isOk()).andReturn());

		mockMvc.perform(get("/api/staff/accounts").header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.username=='administrator')].role").value("ADMINISTRATOR"))
			.andExpect(jsonPath("$[?(@.username=='booking.staff')].role").value("BOOKING_STAFF"))
			.andExpect(jsonPath("$[?(@.username=='booking.staff')].active").value(true));
	}

	@Test
	void duplicateUsernameIsAConflict() throws Exception {
		String adminToken = accessToken(login("administrator", "AdminPassw0rd!").andExpect(status().isOk()).andReturn());

		mockMvc.perform(post("/api/staff/accounts")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"booking.staff","password":"AnotherPassw0rd!"}
						"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("staff.username_conflict"));
	}

	@Test
	void createRejectsShortPasswordsAndMissingPasswords() throws Exception {
		String adminToken = accessToken(login("administrator", "AdminPassw0rd!").andExpect(status().isOk()).andReturn());
		String username = uniqueUsername();

		mockMvc.perform(post("/api/staff/accounts")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"short"}
						""".formatted(username)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("request.invalid"));

		mockMvc.perform(post("/api/staff/accounts")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s"}
						""".formatted(username)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("request.invalid"));

		mockMvc.perform(post("/api/staff/accounts")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"%s"}
						""".formatted(username, "x".repeat(73))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("request.invalid"));

		assertThat(jdbcTemplate.queryForList(
				"select username from cineflow.staff_accounts where username = ?",
				String.class,
				username)).isEmpty();
	}

	@Test
	void bookingStaffCannotAdministerAccounts() throws Exception {
		MvcResult staffLogin = login("booking.staff", "StaffPassw0rd!").andExpect(status().isOk()).andReturn();
		String staffToken = accessToken(staffLogin);
		int staffId = JsonPath.read(staffLogin.getResponse().getContentAsString(), "$.staff.id");

		mockMvc.perform(get("/api/staff/accounts").header("Authorization", "Bearer " + staffToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));

		mockMvc.perform(post("/api/staff/accounts")
				.header("Authorization", "Bearer " + staffToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"intruder.staff","password":"IntruderPass1!"}
						"""))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));

		mockMvc.perform(post("/api/staff/accounts/" + staffId + "/password-reset")
				.header("Authorization", "Bearer " + staffToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"password":"IntruderPass1!"}
						"""))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));
	}

	@Test
	void accountChangesAreAuditedWithoutSecrets() throws Exception {
		String username = uniqueUsername();
		String password = "AuditedPassw0rd!";
		String adminToken = accessToken(login("administrator", "AdminPassw0rd!").andExpect(status().isOk()).andReturn());

		MvcResult created = mockMvc.perform(post("/api/staff/accounts")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"%s"}
						""".formatted(username, password)))
			.andExpect(status().isCreated())
			.andReturn();
		int staffId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(post("/api/staff/accounts/" + staffId + "/password-reset")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"password":"RotatedPassw0rd!"}
						"""))
			.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/staff/accounts/" + staffId + "/deactivate")
				.header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isNoContent());

		List<String> actions = jdbcTemplate.queryForList(
				"select action from cineflow.audit_events where subject_id = ?",
				String.class,
				Integer.toString(staffId));
		assertThat(actions).contains("STAFF_CREATED", "STAFF_PASSWORD_RESET", "STAFF_DEACTIVATED");

		List<String> subjects = jdbcTemplate.queryForList(
				"select coalesce(subject_id, '') || coalesce(subject_type, '') || coalesce(correlation_id, '') from cineflow.audit_events",
				String.class);
		assertThat(subjects).noneMatch(value -> value.contains(password) || value.contains("RotatedPassw0rd!"));
	}

	private ResultActions login(String username, String password) throws Exception {
		return mockMvc.perform(post("/api/auth/login")
			.header("X-Forwarded-For", UUID.randomUUID().toString())
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"%s","password":"%s"}
					""".formatted(username, password)));
	}

	private static String accessToken(MvcResult result) throws Exception {
		return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}

	private static String uniqueUsername() {
		return "staff" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}
