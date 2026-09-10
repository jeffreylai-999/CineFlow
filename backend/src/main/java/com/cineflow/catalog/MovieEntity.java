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

	Long getId() {
		return id;
	}

	String getTitle() {
		return title;
	}

	String getSynopsis() {
		return synopsis;
	}

	String getGenre() {
		return genre;
	}

	int getRuntimeMinutes() {
		return runtimeMinutes;
	}

	String getAgeRating() {
		return ageRating;
	}

	String getPosterUrl() {
		return posterUrl;
	}

	MovieResponse toResponse() {
		return new MovieResponse(id, title, synopsis, genre, runtimeMinutes, ageRating, posterUrl);
	}
}
