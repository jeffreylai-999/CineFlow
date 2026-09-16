package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

	@MockitoBean(name = "tmdbMovieMetadataProvider")
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
			.andExpect(jsonPath("$[0].providerId").value("tmdb"))
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
						{"providerId":"tmdb","externalId":"4242"}
						"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void administratorCanImportWithoutDuplicatingAProviderIdentifier() throws Exception {
		when(movieMetadataProvider.providerId()).thenReturn("tmdb");
		when(movieMetadataProvider.fetch("9001"))
			.thenReturn(providerRecord("9001", "Imported Gate", "Adventure", 101, "PG"))
			.thenThrow(MovieProviderException.unavailable());

		String created = mockMvc.perform(post("/api/admin/movies/import")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"providerId":"tmdb","externalId":"9001"}
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
						{"providerId":"tmdb","externalId":"9001"}
						"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("catalog.duplicate_import"));

		verify(movieMetadataProvider, times(1)).fetch("9001");
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
						{"providerId":"tmdb","externalId":"9002","runtimeMinutes":121,"ageRating":"PG-13"}
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
	void refreshDoesNotOverwriteAConcurrentSchedulingPatch() throws Exception {
		CountDownLatch fetchStarted = new CountDownLatch(1);
		CountDownLatch allowFetch = new CountDownLatch(1);
		when(movieMetadataProvider.providerId()).thenReturn("tmdb");
		when(movieMetadataProvider.fetch("9004"))
			.thenReturn(providerRecord("9004", "Original Race", "Adventure", 110, "PG"))
			.thenAnswer(invocation -> {
				fetchStarted.countDown();
				assertThat(allowFetch.await(5, TimeUnit.SECONDS)).isTrue();
				return providerRecord("9004", "Refreshed Race", "Science Fiction", 999, "R");
			});

		String created = mockMvc.perform(post("/api/admin/movies/import")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"providerId":"tmdb","externalId":"9004","runtimeMinutes":121,"ageRating":"PG-13"}
						"""))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		int movieId = JsonPath.read(created, "$.id");
		String token = adminToken();
		var refresh = Executors.newSingleThreadExecutor();
		try {
			var pending = refresh.submit(() -> mockMvc.perform(post("/api/admin/movies/" + movieId + "/refresh")
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Refreshed Race"))
				.andExpect(jsonPath("$.runtimeMinutes").value(140))
				.andExpect(jsonPath("$.ageRating").value("NC-16")));
			assertThat(fetchStarted.await(5, TimeUnit.SECONDS)).isTrue();
			mockMvc.perform(patch("/api/admin/movies/" + movieId)
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"runtimeMinutes":140,"ageRating":"NC-16"}
							"""))
				.andExpect(status().isOk());
			allowFetch.countDown();
			pending.get(10, TimeUnit.SECONDS);
		}
		finally {
			refresh.shutdownNow();
		}
	}

	@Test
	void importSurfacesProviderQuotaWithoutDetails() throws Exception {
		when(movieMetadataProvider.providerId()).thenReturn("tmdb");
		when(movieMetadataProvider.fetch("4290")).thenThrow(MovieProviderException.quota());

		mockMvc.perform(post("/api/admin/movies/import")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"providerId":"tmdb","externalId":"4290"}
						"""))
			.andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.code").value("catalog.provider_quota"))
			.andExpect(jsonPath("$.detail").doesNotExist())
			.andExpect(header().exists("Retry-After"));
	}

	@Test
	void administratorCannotSelectAnUnconfiguredProvider() throws Exception {
		mockMvc.perform(put("/api/admin/movie-providers/active")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"providerId":"omdb"}
						"""))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.code").value("catalog.provider_not_configured"));
		assertThat(jdbcTemplate.queryForObject(
				"select active_provider from cineflow.catalog_settings where id = 1",
				String.class)).isEqualTo("tmdb");
	}

	@Test
	void portalListsOnlyTheConfiguredProvider() throws Exception {
		mockMvc.perform(get("/api/admin/movie-providers")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.activeProviderId").value("tmdb"))
			.andExpect(jsonPath("$.providers.length()").value(1))
			.andExpect(jsonPath("$.providers[0].id").value("tmdb"))
			.andExpect(jsonPath("$.providers[0].displayName").value("TMDB"))
			.andExpect(content().string(not(containsString("omdb"))))
			.andExpect(content().string(not(containsString("tmdb-token"))));
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

	@Test
	void refreshOfAFixtureMovieIsRejectedWithoutLookingLikeAnOutage() throws Exception {
		Long movieId = jdbcTemplate.queryForObject(
				"select id from cineflow.movies where source_provider = 'fixture' and external_id = 'nebula-express'",
				Long.class);

		mockMvc.perform(post("/api/admin/movies/" + movieId + "/refresh")
				.header("Authorization", "Bearer " + adminToken()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("catalog.invalid_request"));
	}

	private static MovieSearchHit courierHit() {
		return new MovieSearchHit(
				"4242",
				"The Courier Gate",
				"2024",
				"https://image.tmdb.org/t/p/w500/courier-gate.jpg",
				"tmdb");
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

	private String cachedAdminToken;

	private String adminToken() throws Exception {
		return accessToken("administrator", "AdminPassw0rd!");
	}

	private String accessToken(String username, String password) throws Exception {
		if ("administrator".equals(username) && cachedAdminToken != null) {
			return cachedAdminToken;
		}
		String body = mockMvc.perform(post("/api/auth/login")
				.header("X-Forwarded-For", "198.51.100.40")
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
		return token;
	}

	private List<String> auditActions() {
		return jdbcTemplate.queryForList("select action from cineflow.audit_events", String.class);
	}
}
