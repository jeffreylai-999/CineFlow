package com.cineflow.catalog;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
class TmdbMovieMetadataProvider implements MovieMetadataProvider {

	private final RestClient tmdbRestClient;
	private final TmdbProperties properties;

	TmdbMovieMetadataProvider(@Qualifier("tmdbRestClient") RestClient tmdbRestClient, TmdbProperties properties) {
		this.tmdbRestClient = tmdbRestClient;
		this.properties = properties;
	}

	@Override
	public String providerId() {
		return "tmdb";
	}

	@Override
	public List<MovieSearchHit> search(String query) {
		TmdbSearchResponse response = execute(() -> tmdbRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/search/movie").queryParam("query", query).build())
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.accessToken())
			.retrieve()
			.body(TmdbSearchResponse.class));
		if (response == null || response.results() == null) {
			return List.of();
		}
		return response.results().stream().map(this::toHit).toList();
	}

	@Override
	public MovieProviderRecord fetch(String externalId) {
		TmdbMovieDetails details = execute(() -> tmdbRestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/movie/{id}")
				.queryParam("append_to_response", "release_dates")
				.build(externalId))
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.accessToken())
			.retrieve()
			.body(TmdbMovieDetails.class));
		if (details == null) {
			throw MovieProviderException.unavailable();
		}
		return new MovieProviderRecord(
				providerId(),
				Long.toString(details.id()),
				details.title(),
				details.overview() == null ? "" : details.overview(),
				firstGenre(details.genres()),
				posterUrl(details.posterPath()),
				runtime(details.runtime()),
				ageRating(details.releaseDates()));
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
		if (status == HttpStatus.TOO_MANY_REQUESTS.value()) {
			return MovieProviderException.quota();
		}
		return MovieProviderException.unavailable();
	}

	private MovieSearchHit toHit(TmdbSearchResult result) {
		return new MovieSearchHit(
				Long.toString(result.id()),
				result.title(),
				year(result.releaseDate()),
				posterUrl(result.posterPath()));
	}

	private String posterUrl(String posterPath) {
		if (posterPath == null || posterPath.isBlank()) {
			return null;
		}
		return properties.imageBaseUrl() + posterPath;
	}

	private static String year(String releaseDate) {
		if (releaseDate == null || releaseDate.length() < 4) {
			return null;
		}
		return releaseDate.substring(0, 4);
	}

	private static String firstGenre(List<TmdbGenre> genres) {
		if (genres == null || genres.isEmpty() || genres.getFirst().name() == null || genres.getFirst().name().isBlank()) {
			return "Unspecified";
		}
		return genres.getFirst().name();
	}

	private static Integer runtime(Integer runtimeMinutes) {
		if (runtimeMinutes == null || runtimeMinutes <= 0) {
			return null;
		}
		return runtimeMinutes;
	}

	private static String ageRating(TmdbReleaseDates releaseDates) {
		if (releaseDates == null || releaseDates.results() == null) {
			return null;
		}
		String us = certificationFor(releaseDates, "US");
		if (us != null) {
			return us;
		}
		for (TmdbReleaseCountry country : releaseDates.results()) {
			String certification = firstCertification(country);
			if (certification != null) {
				return certification;
			}
		}
		return null;
	}

	private static String certificationFor(TmdbReleaseDates releaseDates, String countryCode) {
		for (TmdbReleaseCountry country : releaseDates.results()) {
			if (countryCode.equals(country.countryCode())) {
				return firstCertification(country);
			}
		}
		return null;
	}

	private static String firstCertification(TmdbReleaseCountry country) {
		if (country.releaseDates() == null) {
			return null;
		}
		for (TmdbReleaseDate date : country.releaseDates()) {
			if (date.certification() != null && !date.certification().isBlank()) {
				return date.certification();
			}
		}
		return null;
	}

	private record TmdbSearchResponse(List<TmdbSearchResult> results) {
	}

	private record TmdbSearchResult(
			long id,
			String title,
			@JsonProperty("release_date") String releaseDate,
			@JsonProperty("poster_path") String posterPath) {
	}

	private record TmdbMovieDetails(
			long id,
			String title,
			String overview,
			Integer runtime,
			List<TmdbGenre> genres,
			@JsonProperty("poster_path") String posterPath,
			@JsonProperty("release_dates") TmdbReleaseDates releaseDates) {
	}

	private record TmdbGenre(String name) {
	}

	private record TmdbReleaseDates(List<TmdbReleaseCountry> results) {
	}

	private record TmdbReleaseCountry(
			@JsonProperty("iso_3166_1") String countryCode,
			@JsonProperty("release_dates") List<TmdbReleaseDate> releaseDates) {
	}

	private record TmdbReleaseDate(String certification) {
	}
}
