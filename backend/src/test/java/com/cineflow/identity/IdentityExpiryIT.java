package com.cineflow.identity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.cineflow.MutableClock;
import com.cineflow.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import({ TestcontainersConfiguration.class, IdentityExpiryIT.ClockConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
class IdentityExpiryIT {

	private static final Instant START = Instant.parse("2026-09-14T00:00:00Z");

	@Autowired
	MockMvc mockMvc;

	@Autowired
	MutableClock clock;

	@Test
	void accessTokenExpiresAfterFifteenMinutesAndRefreshStillWorks() throws Exception {
		clock.set(START);
		MvcResult login = signIn();
		String accessToken = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
		Cookie refresh = login.getResponse().getCookie("cineflow_refresh");

		clock.set(START.plus(Duration.ofMinutes(16)));
		mockMvc.perform(get("/api/staff/me").header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/auth/refresh").cookie(refresh))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isString());
	}

	@Test
	void refreshTokenExpiresAfterEightHours() throws Exception {
		clock.set(START);
		Cookie refresh = signIn().getResponse().getCookie("cineflow_refresh");
		clock.set(START.plus(Duration.ofHours(8)).plus(Duration.ofMinutes(1)));
		mockMvc.perform(post("/api/auth/refresh").cookie(refresh))
			.andExpect(status().isUnauthorized());
	}

	private MvcResult signIn() throws Exception {
		return mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"administrator","password":"AdminPassw0rd!"}
						"""))
			.andExpect(status().isOk())
			.andReturn();
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
