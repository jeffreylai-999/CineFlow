package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MvcResult;

import com.cineflow.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class CatalogMoviesIT {

	@Autowired
	MockMvc mockMvc;

	@Test
	void listsAvailableSanitizedMoviesFromFixture() throws Exception {
		mockMvc.perform(get("/api/movies").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].synopsis").value(
					"A courier crew races a sealed cargo across three colonies before the gate collapses."))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].genre").value("Adventure"))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].runtimeMinutes").value(118))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].ageRating").value("PG-13"))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].posterUrl")
					.value("https://cdn.example.test/posters/nebula-express.jpg"))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].id").exists());
	}

	@Test
	void echoesOrAssignsCorrelationId() throws Exception {
		mockMvc.perform(get("/api/movies").header("X-Correlation-Id", "trace-fixture-1"))
			.andExpect(status().isOk())
			.andExpect(header().string("X-Correlation-Id", "trace-fixture-1"));

		MvcResult generated = mockMvc.perform(get("/api/movies"))
			.andExpect(status().isOk())
			.andExpect(header().exists("X-Correlation-Id"))
			.andReturn();

		assertThat(generated.getResponse().getHeader("X-Correlation-Id")).isNotBlank();
	}

	@Test
	void unknownApiRouteReturnsProblemDetails() throws Exception {
		mockMvc.perform(get("/api/does-not-exist").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(header().exists("X-Correlation-Id"))
			.andExpect(jsonPath("$.title").value("Unauthorized"))
			.andExpect(jsonPath("$.status").value(401))
			.andExpect(jsonPath("$.code").value("auth.unauthorized"));

		String accessToken = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"administrator","password":"AdminPassw0rd!"}
						"""))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		String token = JsonPath.read(accessToken, "$.accessToken");

		mockMvc.perform(get("/api/does-not-exist")
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isNotFound())
			.andExpect(header().exists("X-Correlation-Id"))
			.andExpect(jsonPath("$.title").value("Not Found"))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.code").value("resource.not_found"))
			.andExpect(jsonPath("$.correlationId").exists())
			.andExpect(jsonPath("$.detail").doesNotExist());
	}
}
