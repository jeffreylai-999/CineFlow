package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.cineflow.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"cineflow.tmdb.access-token=",
		"cineflow.omdb.api-key=test-omdb-key"
})
@AutoConfigureMockMvc
class CatalogUnconfiguredActiveProviderIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@MockitoBean(name = "tmdbMovieMetadataProvider")
	MovieMetadataProvider tmdb;

	@MockitoBean(name = "omdbMovieMetadataProvider")
	MovieMetadataProvider omdb;

	@BeforeEach
	void stubProvidersAndResetSelection() {
		when(tmdb.providerId()).thenReturn("tmdb");
		when(omdb.providerId()).thenReturn("omdb");
		jdbcTemplate.update("update cineflow.catalog_settings set active_provider = 'tmdb' where id = 1");
	}

	@Test
	void storedTmdbStaysSelectedUntilAnAdministratorChoosesConfiguredOmdb() throws Exception {
		when(omdb.search("courier gate")).thenReturn(List.of(new MovieSearchHit(
				"tt4242",
				"The Courier Gate",
				"2024",
				"https://img.omdb.test/courier-gate.jpg",
				"omdb")));

		mockMvc.perform(get("/api/admin/movie-providers")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.activeProviderId").value("tmdb"))
			.andExpect(jsonPath("$.providers.length()").value(2))
			.andExpect(jsonPath("$.providers[0].id").value("tmdb"))
			.andExpect(jsonPath("$.providers[1].id").value("omdb"));

		mockMvc.perform(get("/api/admin/movies/search")
				.param("query", "courier gate")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.code").value("catalog.provider_not_configured"));

		assertThat(jdbcTemplate.queryForObject(
				"select active_provider from cineflow.catalog_settings where id = 1",
				String.class)).isEqualTo("tmdb");
		verify(tmdb, never()).search(anyString());
		verify(omdb, never()).search(anyString());

		mockMvc.perform(put("/api/admin/movie-providers/active")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"providerId":"omdb"}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.activeProviderId").value("omdb"));

		mockMvc.perform(get("/api/admin/movies/search")
				.param("query", "courier gate")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].externalId").value("tt4242"));

		verify(omdb).search("courier gate");
		verify(tmdb, never()).search(anyString());
	}

	private String cachedAdminToken;

	private String adminToken() throws Exception {
		if (cachedAdminToken != null) {
			return cachedAdminToken;
		}
		String body = mockMvc.perform(post("/api/auth/login")
				.header("X-Forwarded-For", "198.51.100.42")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"administrator","password":"AdminPassw0rd!"}
						"""))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		cachedAdminToken = JsonPath.read(body, "$.accessToken");
		return cachedAdminToken;
	}
}
