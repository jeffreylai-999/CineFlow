package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
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

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void listsAvailableSanitizedMoviesFromFixture() throws Exception {
		// Shared @SpringBootTest contexts can already have earlier nebula-express Showtimes.
		jdbcTemplate.update(
				"""
						insert into cineflow.showtimes (hall_id, movie_id, starts_at, adult_price_myr, child_price_myr)
						select s.hall_id, s.movie_id, now() + interval '2 days', 28.00, 18.00
						from cineflow.showtimes s
						join cineflow.movies m on m.id = s.movie_id
						where m.source_provider = 'fixture' and m.external_id = 'nebula-express'
						order by s.id
						limit 1
						""");

		mockMvc.perform(get("/api/movies").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].synopsis").value(
					"A courier crew races a sealed cargo across three colonies before the gate collapses."))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].genre").value("Adventure"))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].runtimeMinutes").value(118))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].ageRating").value("PG-13"))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].posterUrl")
					.value("https://cdn.example.test/posters/nebula-express.jpg"))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].id").exists())
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].dates[?(@.cinemaDate=='2099-06-20')].cinemaDate")
					.value(Matchers.hasItem("2099-06-20")))
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')].dates[?(@.cinemaDate=='2099-06-20')].showtimes[0].checkoutOpen")
					.value(Matchers.hasItem(true)));
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
