package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import com.cineflow.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class CatalogAdministrationIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@MockitoBean
	MovieMetadataProvider movieMetadataProvider;

	@Test
	void administratorCanSearchWithoutReceivingProviderCredentials() throws Exception {
		when(movieMetadataProvider.search("courier gate")).thenReturn(List.of(courierHit()));

		mockMvc.perform(get("/api/admin/movies/search")
				.param("query", "courier gate")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].externalId").value("4242"))
			.andExpect(jsonPath("$[0].title").value("The Courier Gate"))
			.andExpect(jsonPath("$[0].year").value("2024"))
			.andExpect(jsonPath("$[0].posterUrl").value("https://image.tmdb.org/t/p/w500/courier-gate.jpg"))
			.andExpect(content().string(not(containsString("tmdb-token"))))
			.andExpect(content().string(not(containsString("CINEFLOW_TMDB"))));
	}

	@Test
	void bookingStaffCannotSearchOrImport() throws Exception {
		String staffToken = accessToken("booking.staff", "StaffPassw0rd!");
		mockMvc.perform(get("/api/admin/movies/search")
				.param("query", "courier gate")
				.header("Authorization", "Bearer " + staffToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("auth.forbidden"));
		mockMvc.perform(post("/api/admin/movies/import")
				.header("Authorization", "Bearer " + staffToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"externalId":"4242"}
						"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void administratorCanImportWithoutDuplicatingAProviderIdentifier() throws Exception {
		when(movieMetadataProvider.providerId()).thenReturn("tmdb");
		when(movieMetadataProvider.fetch("9001")).thenReturn(providerRecord("9001", "Imported Gate", "Adventure", 101, "PG"));

		String created = mockMvc.perform(post("/api/admin/movies/import")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"externalId":"9001"}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.title").value("Imported Gate"))
			.andExpect(jsonPath("$.sourceProvider").value("tmdb"))
			.andExpect(jsonPath("$.externalId").value("9001"))
			.andExpect(jsonPath("$.runtimeMinutes").value(101))
			.andExpect(jsonPath("$.ageRating").value("PG"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		mockMvc.perform(get("/api/movies"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.title=='Imported Gate')]").exists());

		mockMvc.perform(post("/api/admin/movies/import")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"externalId":"9001"}
						"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("catalog.duplicate_import"));

		assertThat(auditActions()).contains("MOVIE_IMPORTED");
		assertThat(created).doesNotContain("tmdb-token");
	}

	@Test
	void refreshUpdatesDescriptiveMetadataAndLeavesLocalSchedulingFields() throws Exception {
		when(movieMetadataProvider.providerId()).thenReturn("tmdb");
		when(movieMetadataProvider.fetch("9002")).thenReturn(
				providerRecord("9002", "Original Gate", "Adventure", 110, "PG"),
				providerRecord("9002", "Refreshed Gate", "Science Fiction", 999, "R"));

		String created = mockMvc.perform(post("/api/admin/movies/import")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"externalId":"9002","runtimeMinutes":121,"ageRating":"PG-13"}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.runtimeMinutes").value(121))
			.andExpect(jsonPath("$.ageRating").value("PG-13"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		int movieId = JsonPath.read(created, "$.id");

		mockMvc.perform(post("/api/admin/movies/" + movieId + "/refresh")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("Refreshed Gate"))
			.andExpect(jsonPath("$.genre").value("Science Fiction"))
			.andExpect(jsonPath("$.runtimeMinutes").value(121))
			.andExpect(jsonPath("$.ageRating").value("PG-13"));

		mockMvc.perform(patch("/api/admin/movies/" + movieId)
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"runtimeMinutes":130,"ageRating":"NC-16"}
						"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.runtimeMinutes").value(130))
			.andExpect(jsonPath("$.ageRating").value("NC-16"))
			.andExpect(jsonPath("$.title").value("Refreshed Gate"));

		assertThat(auditActions()).contains("MOVIE_IMPORTED", "MOVIE_REFRESHED");
	}

	@Test
	void publicCatalogStaysAvailableWhenTheProviderFails() throws Exception {
		when(movieMetadataProvider.search(anyString())).thenThrow(MovieProviderException.unavailable());
		when(movieMetadataProvider.fetch(anyString())).thenThrow(MovieProviderException.unavailable());

		mockMvc.perform(get("/api/movies"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.title=='Nebula Express')]").exists());

		mockMvc.perform(get("/api/admin/movies/search")
				.param("query", "courier gate")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.code").value("catalog.provider_unavailable"))
			.andExpect(jsonPath("$.detail").doesNotExist());
	}

	private static MovieSearchHit courierHit() {
		return new MovieSearchHit(
				"4242",
				"The Courier Gate",
				"2024",
				"https://image.tmdb.org/t/p/w500/courier-gate.jpg");
	}

	private static MovieProviderRecord providerRecord(
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

	private String adminToken() throws Exception {
		return accessToken("administrator", "AdminPassw0rd!");
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

	private List<String> auditActions() {
		return jdbcTemplate.queryForList("select action from cineflow.audit_events", String.class);
	}
}
