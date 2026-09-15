package com.cineflow.catalog;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.cineflow.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"cineflow.catalog.search-rate-limit=2",
		"cineflow.catalog.rate-limit-window=PT1M"
})
@AutoConfigureMockMvc
class CatalogSearchRateLimitIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@MockitoBean(name = "tmdbMovieMetadataProvider")
	MovieMetadataProvider movieMetadataProvider;

	@Test
	void searchIsRateLimitedPerStaffAccount() throws Exception {
		when(movieMetadataProvider.search("courier gate")).thenReturn(List.of());
		String first = accessToken("administrator", "AdminPassw0rd!");
		String second = accessToken(insertSecondAdministrator(), "AdminPassw0rd!");

		search(first, "203.0.113.10").andExpect(status().isOk());
		search(first, "198.51.100.20").andExpect(status().isOk());
		search(first, "198.51.100.20")
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("catalog.rate_limited"))
			.andExpect(header().exists("Retry-After"));
		search(second, "203.0.113.10").andExpect(status().isOk());
	}

	private ResultActions search(String token, String forwardedFor) throws Exception {
		return mockMvc.perform(get("/api/admin/movies/search")
				.param("query", "courier gate")
				.header("Authorization", "Bearer " + token)
				.header("X-Forwarded-For", forwardedFor));
	}

	private String insertSecondAdministrator() {
		String username = "administrator.search";
		String hash = jdbcTemplate.queryForObject(
				"select password_hash from cineflow.staff_accounts where username = ?",
				String.class,
				"administrator");
		Integer existing = jdbcTemplate.queryForObject(
				"select count(*) from cineflow.staff_accounts where username = ?",
				Integer.class,
				username);
		if (existing != null && existing == 0) {
			jdbcTemplate.update(
					"""
							insert into cineflow.staff_accounts (username, password_hash, role, active, created_at)
							values (?, ?, 'ADMINISTRATOR', true, now())
							""",
					username,
					hash);
		}
		return username;
	}

	private String accessToken(String username, String password) throws Exception {
		String body = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"%s"}
						""".formatted(username, password)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return JsonPath.read(body, "$.accessToken");
	}
}
