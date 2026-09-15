package com.cineflow.identity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.cineflow.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"cineflow.auth.login-rate-limit=2",
		"cineflow.auth.refresh-rate-limit=2",
		"cineflow.auth.rate-limit-window=PT1M"
})
@AutoConfigureMockMvc
class IdentityRateLimitIT {

	@Autowired
	MockMvc mockMvc;

	@Test
	void loginIsRateLimited() throws Exception {
		attemptLogin().andExpect(status().isUnauthorized());
		attemptLogin().andExpect(status().isUnauthorized());
		attemptLogin()
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("auth.rate_limited"))
			.andExpect(header().exists("Retry-After"));
	}

	@Test
	void refreshIsRateLimited() throws Exception {
		mockMvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/auth/refresh"))
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("auth.rate_limited"));
	}

	@Test
	void refreshRateLimitUsesTheForwardedClientAddress() throws Exception {
		mockMvc.perform(post("/api/auth/refresh").header("X-Forwarded-For", "203.0.113.10"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/auth/refresh").header("X-Forwarded-For", "203.0.113.10"))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/auth/refresh").header("X-Forwarded-For", "203.0.113.10"))
			.andExpect(status().isTooManyRequests());
		mockMvc.perform(post("/api/auth/refresh").header("X-Forwarded-For", "198.51.100.20"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void loginRateLimitUsesTheForwardedClientAddress() throws Exception {
		attemptLogin("203.0.113.10").andExpect(status().isUnauthorized());
		attemptLogin("203.0.113.10").andExpect(status().isUnauthorized());
		attemptLogin("203.0.113.10").andExpect(status().isTooManyRequests());
		attemptLogin("198.51.100.20").andExpect(status().isUnauthorized());
	}

	private ResultActions attemptLogin() throws Exception {
		return attemptLogin(null);
	}

	private ResultActions attemptLogin(String forwardedFor) throws Exception {
		MockHttpServletRequestBuilder request = post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"administrator","password":"wrong"}
					""");
		if (forwardedFor != null) {
			request.header("X-Forwarded-For", forwardedFor);
		}
		return mockMvc.perform(request);
	}
}
