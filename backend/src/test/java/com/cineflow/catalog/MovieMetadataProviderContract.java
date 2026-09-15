package com.cineflow.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

abstract class MovieMetadataProviderContract {

	abstract MovieMetadataProvider provider();

	abstract String expectedProviderId();

	abstract void givenSearchReturnsKnownMovie();

	abstract void givenDetailsReturnKnownMovie();

	abstract void givenDetailsOmitOptionalFields();

	abstract void givenProviderUnavailable();

	abstract void givenSearchCredentialsRejected();

	abstract void givenQuotaExceeded();

	abstract void givenMovieMissing();

	abstract MovieMetadataProvider unconfiguredProvider();

	@Test
	void searchMapsTitlePosterYearAndExternalId() {
		givenSearchReturnsKnownMovie();
		List<MovieSearchHit> hits = provider().search("courier gate");
		assertThat(hits).containsExactly(new MovieSearchHit(
				"4242",
				"The Courier Gate",
				"2024",
				"https://image.tmdb.org/t/p/w500/courier-gate.jpg"));
	}

	@Test
	void fetchReturnsProvenanceAndDescriptiveMetadata() {
		givenDetailsReturnKnownMovie();
		MovieProviderRecord record = provider().fetch("4242");
		assertThat(record).isEqualTo(new MovieProviderRecord(
				expectedProviderId(),
				"4242",
				"The Courier Gate",
				"A courier crew races a sealed cargo across three colonies.",
				"Adventure",
				"https://image.tmdb.org/t/p/w500/courier-gate.jpg",
				118,
				"PG-13"));
	}

	@Test
	void fetchLeavesAbsentOptionalFieldsEmpty() {
		givenDetailsOmitOptionalFields();
		MovieProviderRecord record = provider().fetch("99");
		assertThat(record).isEqualTo(new MovieProviderRecord(
				expectedProviderId(),
				"99",
				"Untitled Gate",
				"",
				"Unspecified",
				null,
				null,
				null));
	}

	@Test
	void searchFailsSafelyWhenTheProviderIsUnavailable() {
		givenProviderUnavailable();
		assertThatThrownBy(() -> provider().search("courier gate"))
			.isInstanceOf(MovieProviderException.class)
			.extracting(error -> ((MovieProviderException) error).kind())
			.isEqualTo(MovieProviderException.Kind.UNAVAILABLE);
	}

	@Test
	void fetchFailsSafelyWhenQuotaIsExceeded() {
		givenQuotaExceeded();
		assertThatThrownBy(() -> provider().fetch("4242"))
			.isInstanceOf(MovieProviderException.class)
			.extracting(error -> ((MovieProviderException) error).kind())
			.isEqualTo(MovieProviderException.Kind.QUOTA);
	}

	@Test
	void fetchFailsSafelyWhenTheMovieIsMissing() {
		givenMovieMissing();
		assertThatThrownBy(() -> provider().fetch("404"))
			.isInstanceOf(MovieProviderException.class)
			.extracting(error -> ((MovieProviderException) error).kind())
			.isEqualTo(MovieProviderException.Kind.NOT_FOUND);
	}

	@Test
	void searchFailsSafelyWhenCredentialsAreMissing() {
		assertThatThrownBy(() -> unconfiguredProvider().search("courier gate"))
			.isInstanceOf(MovieProviderException.class)
			.extracting(error -> ((MovieProviderException) error).kind())
			.isEqualTo(MovieProviderException.Kind.NOT_CONFIGURED);
	}

	@Test
	void searchFailsSafelyWhenCredentialsAreRejected() {
		givenSearchCredentialsRejected();
		assertThatThrownBy(() -> provider().search("courier gate"))
			.isInstanceOf(MovieProviderException.class)
			.extracting(error -> ((MovieProviderException) error).kind())
			.isEqualTo(MovieProviderException.Kind.NOT_CONFIGURED);
	}
}
