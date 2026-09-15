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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

	@MockitoBean
	MovieMetadataProvider movieMetadataProvider;

	@Test
	void searchIsRateLimitedPerForwardedClient() throws Exception {
		when(movieMetadataProvider.search("courier gate")).thenReturn(List.of());
		String token = accessToken();

		search(token, "203.0.113.10").andExpect(status().isOk());
		search(token, "203.0.113.10").andExpect(status().isOk());
		search(token, "203.0.113.10")
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("catalog.rate_limited"))
			.andExpect(header().exists("Retry-After"));
		search(token, "198.51.100.20").andExpect(status().isOk());
	}

	private org.springframework.test.web.servlet.ResultActions search(String token, String forwardedFor) throws Exception {
		return mockMvc.perform(get("/api/admin/movies/search")
				.param("query", "courier gate")
				.header("Authorization", "Bearer " + token)
				.header("X-Forwarded-For", forwardedFor));
	}

	private String accessToken() throws Exception {
		String body = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"administrator","password":"AdminPassw0rd!"}
						"""))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return JsonPath.read(body, "$.accessToken");
	}
}
