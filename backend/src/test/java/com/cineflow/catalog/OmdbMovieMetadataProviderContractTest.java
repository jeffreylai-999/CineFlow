package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OmdbMovieMetadataProviderContractTest extends MovieMetadataProviderContract {

	private static final OmdbProperties PROPERTIES = new OmdbProperties(
			"test-omdb-key",
			"https://www.omdbapi.com",
			null,
			null);

	private static final String SEARCH_BODY = """
			{
			  "Search": [
			    {
			      "Title": "The Courier Gate",
			      "Year": "2024",
			      "imdbID": "4242",
			      "Type": "movie",
			      "Poster": "https://image.tmdb.org/t/p/w500/courier-gate.jpg"
			    }
			  ],
			  "totalResults": "1",
			  "Response": "True"
			}
			""";

	private static final String DETAILS_BODY = """
			{
			  "Title": "The Courier Gate",
			  "Year": "2024",
			  "Rated": "PG-13",
			  "Runtime": "118 min",
			  "Genre": "Adventure, Sci-Fi",
			  "Plot": "A courier crew races a sealed cargo across three colonies.",
			  "Poster": "https://image.tmdb.org/t/p/w500/courier-gate.jpg",
			  "imdbID": "4242",
			  "Response": "True"
			}
			""";

	private MockRestServiceServer omdb;
	private MovieMetadataProvider provider;

	@BeforeEach
	void createProvider() {
		RestClient.Builder builder = RestClient.builder().baseUrl(PROPERTIES.baseUrl());
		omdb = MockRestServiceServer.bindTo(builder).build();
		provider = new OmdbMovieMetadataProvider(builder.build(), PROPERTIES);
	}

	@Override
	MovieMetadataProvider provider() {
		return provider;
	}

	@Override
	String expectedProviderId() {
		return "omdb";
	}

	@Override
	void givenSearchReturnsKnownMovie() {
		omdb.expect(requestTo("https://www.omdbapi.com?apikey=test-omdb-key&type=movie&s=courier%20gate"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(SEARCH_BODY, MediaType.APPLICATION_JSON));
	}

	@Override
	MovieMetadataProvider unconfiguredProvider() {
		return new OmdbMovieMetadataProvider(
				RestClient.builder().baseUrl(PROPERTIES.baseUrl()).build(),
				new OmdbProperties("", PROPERTIES.baseUrl(), null, null));
	}

	@Override
	void givenProviderUnavailable() {
		omdb.expect(requestTo("https://www.omdbapi.com?apikey=test-omdb-key&type=movie&s=courier%20gate"))
			.andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
	}

	@Override
	void givenSearchCredentialsRejected() {
		omdb.expect(requestTo("https://www.omdbapi.com?apikey=test-omdb-key&type=movie&s=courier%20gate"))
			.andRespond(withSuccess("""
					{"Response":"False","Error":"Invalid API key!"}
					""", MediaType.APPLICATION_JSON));
	}

	@Override
	void givenQuotaExceeded() {
		omdb.expect(requestTo("https://www.omdbapi.com?apikey=test-omdb-key&i=4242&plot=full"))
			.andRespond(withSuccess("""
					{"Response":"False","Error":"Request limit reached!"}
					""", MediaType.APPLICATION_JSON));
	}

	@Override
	void givenMovieMissing() {
		omdb.expect(requestTo("https://www.omdbapi.com?apikey=test-omdb-key&i=404&plot=full"))
			.andRespond(withSuccess("""
					{"Response":"False","Error":"Movie not found!"}
					""", MediaType.APPLICATION_JSON));
	}

	@Override
	void givenDetailsOmitOptionalFields() {
		omdb.expect(requestTo("https://www.omdbapi.com?apikey=test-omdb-key&i=99&plot=full"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("""
					{
					  "Title": "Untitled Gate",
					  "Year": "N/A",
					  "Rated": "N/A",
					  "Runtime": "N/A",
					  "Genre": "N/A",
					  "Plot": "N/A",
					  "Poster": "N/A",
					  "imdbID": "99",
					  "Response": "True"
					}
					""", MediaType.APPLICATION_JSON));
	}

	@Override
	void givenDetailsReturnKnownMovie() {
		omdb.expect(requestTo("https://www.omdbapi.com?apikey=test-omdb-key&i=4242&plot=full"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(DETAILS_BODY, MediaType.APPLICATION_JSON));
	}

	@Test
	void searchTreatsNoMatchesAsEmptyResults() {
		omdb.expect(requestTo("https://www.omdbapi.com?apikey=test-omdb-key&type=movie&s=courier%20gate"))
			.andRespond(withSuccess("""
					{"Response":"False","Error":"Movie not found!"}
					""", MediaType.APPLICATION_JSON));
		assertThat(provider().search("courier gate")).isEmpty();
	}
}
