package com.cineflow.catalog;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
class OmdbMovieMetadataProvider implements MovieMetadataProvider {

	private final RestClient omdbRestClient;
	private final OmdbProperties properties;

	OmdbMovieMetadataProvider(@Qualifier("omdbRestClient") RestClient omdbRestClient, OmdbProperties properties) {
		this.omdbRestClient = omdbRestClient;
		this.properties = properties;
	}

	@Override
	public String providerId() {
		return "omdb";
	}

	@Override
	public List<MovieSearchHit> search(String query) {
		OmdbSearchResponse response = execute(() -> omdbRestClient.get()
			.uri(uriBuilder -> uriBuilder
				.queryParam("apikey", properties.apiKey())
				.queryParam("type", "movie")
				.queryParam("s", query)
				.build())
			.retrieve()
			.body(OmdbSearchResponse.class));
		if (response == null) {
			throw MovieProviderException.unavailable();
		}
		if (!response.succeeded() || response.results() == null) {
			MovieProviderException mapped = mapError(response.error());
			if (mapped.kind() == MovieProviderException.Kind.NOT_FOUND) {
				return List.of();
			}
			throw mapped;
		}
		return response.results().stream().map(this::toHit).toList();
	}

	@Override
	public MovieProviderRecord fetch(String externalId) {
		OmdbMovieDetails details = execute(() -> omdbRestClient.get()
			.uri(uriBuilder -> uriBuilder
				.queryParam("apikey", properties.apiKey())
				.queryParam("i", externalId)
				.queryParam("plot", "full")
				.build())
			.retrieve()
			.body(OmdbMovieDetails.class));
		if (details == null) {
			throw MovieProviderException.unavailable();
		}
		if (!details.succeeded()) {
			throw mapError(details.error());
		}
		return new MovieProviderRecord(
				providerId(),
				text(details.imdbId(), externalId),
				text(details.title(), "Untitled"),
				isBlankOrNa(details.plot()) ? "" : details.plot(),
				firstGenre(details.genre()),
				optionalUrl(details.poster()),
				runtime(details.runtime()),
				optionalText(details.rated()));
	}

	private <T> T execute(Supplier<T> call) {
		if (!properties.configured()) {
			throw MovieProviderException.notConfigured();
		}
		try {
			return call.get();
		}
		catch (RestClientResponseException exception) {
			throw mapStatus(exception.getStatusCode().value());
		}
		catch (RestClientException exception) {
			throw MovieProviderException.unavailable();
		}
	}

	private static MovieProviderException mapStatus(int status) {
		if (status == HttpStatus.NOT_FOUND.value()) {
			return MovieProviderException.notFound();
		}
		if (status == HttpStatus.TOO_MANY_REQUESTS.value() || status == HttpStatus.UNAUTHORIZED.value()) {
			return status == HttpStatus.TOO_MANY_REQUESTS.value()
					? MovieProviderException.quota()
					: MovieProviderException.notConfigured();
		}
		return MovieProviderException.unavailable();
	}

	private static MovieProviderException mapError(String error) {
		if (error == null) {
			return MovieProviderException.unavailable();
		}
		String message = error.toLowerCase(Locale.ROOT);
		if (message.contains("not found")) {
			return MovieProviderException.notFound();
		}
		if (message.contains("limit")) {
			return MovieProviderException.quota();
		}
		if (message.contains("invalid api key")) {
			return MovieProviderException.notConfigured();
		}
		return MovieProviderException.unavailable();
	}

	private MovieSearchHit toHit(OmdbSearchResult result) {
		return new MovieSearchHit(
				result.imdbId(),
				result.title(),
				year(result.year()),
				optionalUrl(result.poster()),
				providerId());
	}

	private static String year(String year) {
		if (isBlankOrNa(year) || year.length() < 4) {
			return null;
		}
		return year.substring(0, 4);
	}

	private static String firstGenre(String genre) {
		if (isBlankOrNa(genre)) {
			return "Unspecified";
		}
		int comma = genre.indexOf(',');
		String first = comma < 0 ? genre : genre.substring(0, comma);
		String trimmed = first.trim();
		return trimmed.isEmpty() ? "Unspecified" : trimmed;
	}

	private static Integer runtime(String runtime) {
		if (isBlankOrNa(runtime)) {
			return null;
		}
		int space = runtime.indexOf(' ');
		String minutes = space < 0 ? runtime : runtime.substring(0, space);
		try {
			int value = Integer.parseInt(minutes);
			return value > 0 ? value : null;
		}
		catch (NumberFormatException exception) {
			return null;
		}
	}

	private static String optionalText(String value) {
		return isBlankOrNa(value) ? null : value;
	}

	private static String optionalUrl(String value) {
		return isBlankOrNa(value) ? null : value;
	}

	private static String text(String value, String fallback) {
		return isBlankOrNa(value) ? fallback : value;
	}

	private static boolean isBlankOrNa(String value) {
		return value == null || value.isBlank() || "N/A".equalsIgnoreCase(value);
	}

	private record OmdbSearchResponse(
			@JsonProperty("Search") List<OmdbSearchResult> results,
			@JsonProperty("Response") String response,
			@JsonProperty("Error") String error) {

		boolean succeeded() {
			return "True".equalsIgnoreCase(response);
		}
	}

	private record OmdbSearchResult(
			@JsonProperty("Title") String title,
			@JsonProperty("Year") String year,
			@JsonProperty("imdbID") String imdbId,
			@JsonProperty("Poster") String poster) {
	}

	private record OmdbMovieDetails(
			@JsonProperty("Title") String title,
			@JsonProperty("Plot") String plot,
			@JsonProperty("Genre") String genre,
			@JsonProperty("Poster") String poster,
			@JsonProperty("Runtime") String runtime,
			@JsonProperty("Rated") String rated,
			@JsonProperty("imdbID") String imdbId,
			@JsonProperty("Response") String response,
			@JsonProperty("Error") String error) {

		boolean succeeded() {
			return "True".equalsIgnoreCase(response);
		}
	}
}
