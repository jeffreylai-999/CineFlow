package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
		"cineflow.tmdb.access-token=test-tmdb-token",
		"cineflow.omdb.api-key=test-omdb-key"
})
@AutoConfigureMockMvc
class CatalogProviderSelectionIT {

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
	void administratorCanSelectOmdbAndNewSearchUsesThatProvider() throws Exception {
		when(omdb.search("courier gate")).thenReturn(List.of(new MovieSearchHit(
				"tt4242",
				"The Courier Gate",
				"2024",
				"https://img.omdb.test/courier-gate.jpg")));

		mockMvc.perform(get("/api/admin/movie-providers")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.activeProviderId").value("tmdb"))
			.andExpect(jsonPath("$.providers.length()").value(2))
			.andExpect(jsonPath("$.providers[1].id").value("omdb"))
			.andExpect(jsonPath("$.providers[1].displayName").value("OMDb"));

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
			.andExpect(jsonPath("$[0].externalId").value("tt4242"))
			.andExpect(jsonPath("$[0].title").value("The Courier Gate"));

		verify(omdb).search("courier gate");
		verify(tmdb, never()).search(anyString());
		assertThat(auditActions()).contains("MOVIE_PROVIDER_SELECTED");
		assertThat(jdbcTemplate.queryForObject(
				"select active_provider from cineflow.catalog_settings where id = 1",
				String.class)).isEqualTo("omdb");
	}

	@Test
	void refreshUsesTheMovieSourceAfterTheActiveProviderChanges() throws Exception {
		when(tmdb.fetch("9008")).thenReturn(
				tmdbRecord("9008", "Original Gate", "Adventure", 110, "PG"),
				tmdbRecord("9008", "Refreshed Gate", "Science Fiction", 999, "R"));
		when(omdb.fetch(anyString())).thenThrow(MovieProviderException.unavailable());

		String created = mockMvc.perform(post("/api/admin/movies/import")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"externalId":"9008","runtimeMinutes":121,"ageRating":"PG-13"}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.sourceProvider").value("tmdb"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		int movieId = JsonPath.read(created, "$.id");

		mockMvc.perform(put("/api/admin/movie-providers/active")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"providerId":"omdb"}
						"""))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/admin/movies/" + movieId + "/refresh")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Refreshed Gate"))
			.andExpect(jsonPath("$.sourceProvider").value("tmdb"))
			.andExpect(jsonPath("$.runtimeMinutes").value(121))
			.andExpect(jsonPath("$.ageRating").value("PG-13"));

		verify(tmdb, times(2)).fetch("9008");
		verify(omdb, never()).fetch(anyString());
	}

	@Test
	void selectingAnUnknownProviderIsRejectedAndDoesNotFailOver() throws Exception {
		when(tmdb.search("courier gate")).thenReturn(List.of());

		mockMvc.perform(put("/api/admin/movie-providers/active")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"providerId":"imdb"}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("catalog.invalid_request"));

		mockMvc.perform(get("/api/admin/movies/search")
				.param("query", "courier gate")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isOk());
		verify(tmdb).search("courier gate");
		verify(omdb, never()).search(anyString());
	}

	@Test
	void bookingStaffCannotSelectAProvider() throws Exception {
		String staffToken = accessToken("booking.staff", "StaffPassw0rd!");
		mockMvc.perform(get("/api/admin/movie-providers").header("Authorization", "Bearer " + staffToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));
		mockMvc.perform(put("/api/admin/movie-providers/active")
				.header("Authorization", "Bearer " + staffToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"providerId":"omdb"}
						"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void omdbOutageDoesNotHideExistingMovies() throws Exception {
		when(omdb.search(anyString())).thenThrow(MovieProviderException.unavailable());

		mockMvc.perform(put("/api/admin/movie-providers/active")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"providerId":"omdb"}
						"""))
			.andExpect(status().isOk());

		mockMvc.perform(get("/api/movies"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')]").exists());

		mockMvc.perform(get("/api/admin/movies")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')]").exists());

		mockMvc.perform(get("/api/admin/movies/search")
				.param("query", "courier gate")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.code").value("catalog.provider_unavailable"));
	}

	private static MovieProviderRecord tmdbRecord(
			String externalId,
			String title,
			String genre,
			int runtimeMinutes,
			String ageRating) {
		return new MovieProviderRecord(
				"tmdb",
				externalId,
				title,
				"A courier crew races a sealed cargo across three colonies.",
				genre,
				"https://image.tmdb.org/t/p/w500/courier-gate.jpg",
				runtimeMinutes,
				ageRating);
	}

	private String cachedAdminToken;

	private String cachedStaffToken;

	private String adminToken() throws Exception {
		return accessToken("administrator", "AdminPassw0rd!");
	}

	private String accessToken(String username, String password) throws Exception {
		if ("administrator".equals(username) && cachedAdminToken != null) {
			return cachedAdminToken;
		}
		if ("booking.staff".equals(username) && cachedStaffToken != null) {
			return cachedStaffToken;
		}
		String body = mockMvc.perform(post("/api/auth/login")
				.header("X-Forwarded-For", "198.51.100.41")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"%s"}
						""".formatted(username, password)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		String token = JsonPath.read(body, "$.accessToken");
		if ("administrator".equals(username)) {
			cachedAdminToken = token;
		}
		if ("booking.staff".equals(username)) {
			cachedStaffToken = token;
		}
		return token;
	}

	private List<String> auditActions() {
		return jdbcTemplate.queryForList("select action from cineflow.audit_events", String.class);
	}
}
