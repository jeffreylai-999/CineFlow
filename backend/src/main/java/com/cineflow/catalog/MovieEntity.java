package com.cineflow.catalog;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "movies", schema = "cineflow")
class MovieEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String synopsis;

	@Column(nullable = false)
	private String genre;

	@Column(name = "runtime_minutes", nullable = false)
	private int runtimeMinutes;

	@Column(name = "age_rating", nullable = false)
	private String ageRating;

	@Column(name = "poster_url")
	private String posterUrl;

	@Column(name = "source_provider", nullable = false)
	private String sourceProvider;

	@Column(name = "external_id", nullable = false)
	private String externalId;

	@Column(name = "source_refreshed_at", nullable = false)
	private Instant sourceRefreshedAt;

	@Column(name = "archived_at")
	private Instant archivedAt;

	protected MovieEntity() {
	}

	static MovieEntity imported(
			String title,
			String synopsis,
			String genre,
			int runtimeMinutes,
			String ageRating,
			String posterUrl,
			String sourceProvider,
			String externalId,
			Instant sourceRefreshedAt) {
		MovieEntity movie = new MovieEntity();
		movie.title = title;
		movie.synopsis = synopsis;
		movie.genre = genre;
		movie.runtimeMinutes = runtimeMinutes;
		movie.ageRating = ageRating;
		movie.posterUrl = posterUrl;
		movie.sourceProvider = sourceProvider;
		movie.externalId = externalId;
		movie.sourceRefreshedAt = sourceRefreshedAt;
		return movie;
	}

	Long getId() {
		return id;
	}

	String getSourceProvider() {
		return sourceProvider;
	}

	String getExternalId() {
		return externalId;
	}

	void refreshDescriptive(String title, String synopsis, String genre, String posterUrl, Instant sourceRefreshedAt) {
		this.title = title;
		this.synopsis = synopsis;
		this.genre = genre;
		this.posterUrl = posterUrl;
		this.sourceRefreshedAt = sourceRefreshedAt;
	}

	void updateScheduling(int runtimeMinutes, String ageRating) {
		this.runtimeMinutes = runtimeMinutes;
		this.ageRating = ageRating;
	}

	MovieResponse toResponse() {
		return new MovieResponse(id, title, synopsis, genre, runtimeMinutes, ageRating, posterUrl);
	}

	MovieAdminResponse toAdminResponse() {
		return new MovieAdminResponse(
				id,
				title,
				synopsis,
				genre,
				runtimeMinutes,
				ageRating,
				posterUrl,
				sourceProvider,
				externalId,
				sourceRefreshedAt);
	}
}
