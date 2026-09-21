package com.cineflow.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
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

import jakarta.servlet.http.Cookie;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class IdentityAuthIT {

	private static final String BOOKING_STAFF_HASH =
			"$2b$12$fjuTfnbHHQpXBdhjGl6NZ.j9dyjTLrCRr87JskFhkr1cUduAbhW6S";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void bootstrapAdministratorCanLogIn() throws Exception {
		MvcResult result = login("administrator", "AdminPassw0rd!")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isString())
			.andExpect(jsonPath("$.expiresInSeconds").value(900))
			.andExpect(jsonPath("$.staff.username").value("administrator"))
			.andExpect(jsonPath("$.staff.role").value("ADMINISTRATOR"))
			.andExpect(cookie().exists("cineflow_refresh"))
			.andExpect(cookie().httpOnly("cineflow_refresh", true))
			.andExpect(cookie().path("cineflow_refresh", "/api/auth"))
			.andExpect(cookie().maxAge("cineflow_refresh", 8 * 60 * 60))
			.andReturn();

		Cookie refresh = result.getResponse().getCookie("cineflow_refresh");
		assertThat(refresh).isNotNull();
		assertThat(result.getResponse().getContentAsString()).doesNotContain(refresh.getValue());
		assertThat(result.getResponse().getHeader("Set-Cookie")).contains("SameSite=Lax");
		assertThat(accessTokenLifetimeSeconds(json(result, "$.accessToken"))).isEqualTo(900);
	}

	@Test
	void bookingStaffFixtureCanLogIn() throws Exception {
		login("booking.staff", "StaffPassw0rd!")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.staff.username").value("booking.staff"))
			.andExpect(jsonPath("$.staff.role").value("BOOKING_STAFF"));
	}

	@Test
	void rejectedLoginDoesNotRevealWhetherTheUsernameExists() throws Exception {
		login("administrator", "wrong-password")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("auth.invalid_credentials"));
		login("nobody", "StaffPassw0rd!")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("auth.invalid_credentials"));
		assertThat(auditActions()).contains("STAFF_LOGIN_FAILURE");
	}

	@Test
	void refreshRotatesTheCookieAndStoresOnlyHashes() throws Exception {
		MvcResult login = login("administrator", "AdminPassw0rd!").andExpect(status().isOk()).andReturn();
		Cookie first = refreshCookie(login);
		String firstAccess = json(login, "$.accessToken");

		MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh").cookie(first))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isString())
			.andExpect(jsonPath("$.staff.role").value("ADMINISTRATOR"))
			.andReturn();
		Cookie second = refreshCookie(refreshed);
		assertThat(second.getValue()).isNotEqualTo(first.getValue());
		assertThat(json(refreshed, "$.accessToken")).isNotEqualTo(firstAccess);
		assertThat(hashes()).contains(sha256(first.getValue()), sha256(second.getValue()));
		assertThat(hashes()).doesNotContain(first.getValue(), second.getValue());
	}

	@Test
	void reusedRefreshTokenRevokesTheFamily() throws Exception {
		MvcResult login = login("administrator", "AdminPassw0rd!").andExpect(status().isOk()).andReturn();
		Cookie first = refreshCookie(login);
		MvcResult rotated = mockMvc.perform(post("/api/auth/refresh").cookie(first)).andExpect(status().isOk()).andReturn();
		Cookie second = refreshCookie(rotated);

		mockMvc.perform(post("/api/auth/refresh").cookie(first))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("auth.token_reused"));

		mockMvc.perform(post("/api/auth/refresh").cookie(second))
			.andExpect(status().isUnauthorized());

		assertThat(auditActions()).contains("TOKEN_REUSE");
	}

	@Test
	void refreshTokenCleanupPreservesReuseDetectionForRotatedTombstones() throws Exception {
		MvcResult login = login("administrator", "AdminPassw0rd!").andExpect(status().isOk()).andReturn();
		Cookie first = refreshCookie(login);
		MvcResult rotated = mockMvc.perform(post("/api/auth/refresh").cookie(first)).andExpect(status().isOk()).andReturn();
		Cookie second = refreshCookie(rotated);

		jdbcTemplate.update(
				"""
						update cineflow.refresh_tokens
						set expires_at = now() - interval '1 minute'
						where token_hash = ?
						""",
				sha256(first.getValue()));

		Integer removed = jdbcTemplate.queryForObject(
				"select cineflow.cleanup_expired_refresh_tokens(now())",
				Integer.class);
		assertThat(removed).isNotNull();
		assertThat(removed).isZero();
		assertThat(jdbcTemplate.queryForObject(
						"select count(*) from cineflow.refresh_tokens where token_hash = ?",
						Integer.class,
						sha256(first.getValue())))
				.isOne();

		mockMvc.perform(post("/api/auth/refresh").cookie(first))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("auth.token_reused"));

		mockMvc.perform(post("/api/auth/refresh").cookie(second))
			.andExpect(status().isUnauthorized());
		assertThat(auditActions()).contains("TOKEN_REUSE");
	}

	@Test
	void logoutRevokesTheCurrentFamily() throws Exception {
		MvcResult login = login("booking.staff", "StaffPassw0rd!").andExpect(status().isOk()).andReturn();
		Cookie refresh = refreshCookie(login);

		mockMvc.perform(post("/api/auth/logout").cookie(refresh))
			.andExpect(status().isNoContent())
			.andExpect(cookie().maxAge("cineflow_refresh", 0));

		mockMvc.perform(post("/api/auth/refresh").cookie(refresh))
			.andExpect(status().isUnauthorized());
		assertThat(auditActions()).contains("STAFF_LOGOUT");
	}

	@Test
	void accessTokenAuthorizesTheStaffProfile() throws Exception {
		MvcResult login = login("booking.staff", "StaffPassw0rd!").andExpect(status().isOk()).andReturn();
		String accessToken = json(login, "$.accessToken");

		mockMvc.perform(get("/api/staff/me").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.username").value("booking.staff"))
			.andExpect(jsonPath("$.role").value("BOOKING_STAFF"));

		mockMvc.perform(get("/api/staff/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("auth.unauthorized"));
	}

	@Test
	void bookingStaffCannotCallAdministratorOperations() throws Exception {
		MvcResult staffLogin = login("booking.staff", "StaffPassw0rd!").andExpect(status().isOk()).andReturn();
		String staffToken = json(staffLogin, "$.accessToken");
		int staffId = JsonPath.read(staffLogin.getResponse().getContentAsString(), "$.staff.id");

		mockMvc.perform(post("/api/staff/accounts/" + staffId + "/deactivate")
				.header("Authorization", "Bearer " + staffToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));
	}

	@Test
	void passwordResetRevokesExistingSessions() throws Exception {
		String username = "reset." + UUID.randomUUID();
		long staffId = insertBookingStaff(username);
		MvcResult staffLogin = login(username, "StaffPassw0rd!").andExpect(status().isOk()).andReturn();
		Cookie staffRefresh = refreshCookie(staffLogin);

		MvcResult adminLogin = login("administrator", "AdminPassw0rd!").andExpect(status().isOk()).andReturn();
		String adminToken = json(adminLogin, "$.accessToken");

		mockMvc.perform(post("/api/staff/accounts/" + staffId + "/password-reset")
				.header("Authorization", "Bearer " + adminToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"password":"ResetPassw0rd!"}
						"""))
			.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/refresh").cookie(staffRefresh))
			.andExpect(status().isUnauthorized());
		login(username, "StaffPassw0rd!")
			.andExpect(status().isUnauthorized());
		login(username, "ResetPassw0rd!")
			.andExpect(status().isOk());
		assertThat(auditActions()).contains("STAFF_PASSWORD_RESET");
	}

	@Test
	void deactivationStopsLoginAndRefresh() throws Exception {
		String username = "inactive." + UUID.randomUUID();
		long staffId = insertBookingStaff(username);
		MvcResult staffLogin = login(username, "StaffPassw0rd!").andExpect(status().isOk()).andReturn();
		Cookie staffRefresh = refreshCookie(staffLogin);

		MvcResult adminLogin = login("administrator", "AdminPassw0rd!").andExpect(status().isOk()).andReturn();
		String adminToken = json(adminLogin, "$.accessToken");

		mockMvc.perform(post("/api/staff/accounts/" + staffId + "/deactivate")
				.header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/refresh").cookie(staffRefresh))
			.andExpect(status().isUnauthorized());
		login(username, "StaffPassw0rd!")
			.andExpect(status().isUnauthorized());
		assertThat(auditActions()).contains("STAFF_DEACTIVATED");
	}

	private ResultActions login(String username, String password) throws Exception {
		return mockMvc.perform(post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"%s","password":"%s"}
					""".formatted(username, password)));
	}

	private static Cookie refreshCookie(MvcResult result) {
		Cookie cookie = result.getResponse().getCookie("cineflow_refresh");
		assertThat(cookie).isNotNull();
		return cookie;
	}

	private static String json(MvcResult result, String path) throws Exception {
		return JsonPath.read(result.getResponse().getContentAsString(), path);
	}

	private static long accessTokenLifetimeSeconds(String accessToken) {
		String payload = new String(Base64.getUrlDecoder().decode(accessToken.split("\\.")[1]), StandardCharsets.UTF_8);
		long iat = ((Number) JsonPath.read(payload, "$.iat")).longValue();
		long exp = ((Number) JsonPath.read(payload, "$.exp")).longValue();
		return exp - iat;
	}

	private List<String> hashes() {
		return jdbcTemplate.queryForList("select token_hash from cineflow.refresh_tokens", String.class);
	}

	private List<String> auditActions() {
		return jdbcTemplate.queryForList("select action from cineflow.audit_events", String.class);
	}

	private long insertBookingStaff(String username) {
		jdbcTemplate.update(
				"""
						insert into cineflow.staff_accounts (username, password_hash, role, active, created_at)
						values (?, ?, 'BOOKING_STAFF', true, now())
						""",
				username,
				BOOKING_STAFF_HASH);
		return jdbcTemplate.queryForObject(
				"select id from cineflow.staff_accounts where username = ?",
				Long.class,
				username);
	}

	private static String sha256(String value) throws Exception {
		byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
		return HexFormat.of().formatHex(digest);
	}
}
