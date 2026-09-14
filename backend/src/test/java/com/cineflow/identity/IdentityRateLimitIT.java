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

	private ResultActions attemptLogin() throws Exception {
		return mockMvc.perform(post("/api/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"administrator","password":"wrong"}
					"""));
	}
}
