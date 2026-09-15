package com.cineflow.catalog;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TmdbMovieMetadataProviderContractTest extends MovieMetadataProviderContract {

	private static final TmdbProperties PROPERTIES = new TmdbProperties(
			"test-tmdb-token",
			"https://api.themoviedb.org/3",
			"https://image.tmdb.org/t/p/w500",
			null,
			null);

	private static final String SEARCH_BODY = """
			{
			  "results": [
			    {
			      "id": 4242,
			      "title": "The Courier Gate",
			      "overview": "A courier crew races a sealed cargo across three colonies.",
			      "release_date": "2024-03-01",
			      "poster_path": "/courier-gate.jpg"
			    }
			  ]
			}
			""";

	private static final String DETAILS_BODY = """
			{
			  "id": 4242,
			  "title": "The Courier Gate",
			  "overview": "A courier crew races a sealed cargo across three colonies.",
			  "runtime": 118,
			  "genres": [{"id": 12, "name": "Adventure"}],
			  "poster_path": "/courier-gate.jpg",
			  "release_dates": {
			    "results": [
			      {
			        "iso_3166_1": "US",
			        "release_dates": [{"certification": "PG-13"}]
			      }
			    ]
			  }
			}
			""";

	private MockRestServiceServer tmdb;
	private MovieMetadataProvider provider;

	@BeforeEach
	void createProvider() {
		RestClient.Builder builder = RestClient.builder().baseUrl(PROPERTIES.baseUrl());
		tmdb = MockRestServiceServer.bindTo(builder).build();
		provider = new TmdbMovieMetadataProvider(builder.build(), PROPERTIES);
	}

	@Override
	MovieMetadataProvider provider() {
		return provider;
	}

	@Override
	String expectedProviderId() {
		return "tmdb";
	}

	@Override
	void givenSearchReturnsKnownMovie() {
		tmdb.expect(requestTo("https://api.themoviedb.org/3/search/movie?query=courier%20gate"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-tmdb-token"))
			.andRespond(withSuccess(SEARCH_BODY, MediaType.APPLICATION_JSON));
	}

	@Override
	MovieMetadataProvider unconfiguredProvider() {
		return new TmdbMovieMetadataProvider(
				RestClient.builder().baseUrl(PROPERTIES.baseUrl()).build(),
				new TmdbProperties("", PROPERTIES.baseUrl(), PROPERTIES.imageBaseUrl(), null, null));
	}

	@Override
	void givenProviderUnavailable() {
		tmdb.expect(requestTo("https://api.themoviedb.org/3/search/movie?query=courier%20gate"))
			.andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
	}

	@Override
	void givenSearchCredentialsRejected() {
		tmdb.expect(requestTo("https://api.themoviedb.org/3/search/movie?query=courier%20gate"))
			.andRespond(withStatus(HttpStatus.UNAUTHORIZED));
	}

	@Override
	void givenQuotaExceeded() {
		tmdb.expect(requestTo("https://api.themoviedb.org/3/movie/4242?append_to_response=release_dates"))
			.andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
	}

	@Override
	void givenMovieMissing() {
		tmdb.expect(requestTo("https://api.themoviedb.org/3/movie/404?append_to_response=release_dates"))
			.andRespond(withStatus(HttpStatus.NOT_FOUND));
	}

	@Override
	void givenDetailsOmitOptionalFields() {
		tmdb.expect(requestTo("https://api.themoviedb.org/3/movie/99?append_to_response=release_dates"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-tmdb-token"))
			.andRespond(withSuccess("""
					{
					  "id": 99,
					  "title": "Untitled Gate",
					  "overview": null,
					  "runtime": 0,
					  "genres": [],
					  "poster_path": null,
					  "release_dates": { "results": [] }
					}
					""", MediaType.APPLICATION_JSON));
	}

	@Override
	void givenDetailsReturnKnownMovie() {
		tmdb.expect(requestTo("https://api.themoviedb.org/3/movie/4242?append_to_response=release_dates"))
			.andExpect(method(HttpMethod.GET))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-tmdb-token"))
			.andRespond(withSuccess(DETAILS_BODY, MediaType.APPLICATION_JSON));
	}
}
