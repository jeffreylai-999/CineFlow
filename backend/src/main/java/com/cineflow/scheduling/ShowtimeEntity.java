package com.cineflow.scheduling;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "showtimes", schema = "cineflow")
class ShowtimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "hall_id", nullable = false)
	private Long hallId;

	@Column(name = "movie_id", nullable = false)
	private Long movieId;

	@Column(name = "starts_at", nullable = false)
	private Instant startsAt;

	@Column(name = "adult_price_myr", nullable = false)
	private BigDecimal adultPriceMyr;

	@Column(name = "child_price_myr", nullable = false)
	private BigDecimal childPriceMyr;

	protected ShowtimeEntity() {
	}

	ShowtimeEntity(
			Long hallId,
			Long movieId,
			Instant startsAt,
			BigDecimal adultPriceMyr,
			BigDecimal childPriceMyr) {
		this.hallId = hallId;
		this.movieId = movieId;
		this.startsAt = startsAt;
		this.adultPriceMyr = adultPriceMyr;
		this.childPriceMyr = childPriceMyr;
	}

	Long getId() {
		return id;
	}

	Long getHallId() {
		return hallId;
	}

	Long getMovieId() {
		return movieId;
	}

	Instant getStartsAt() {
		return startsAt;
	}

	BigDecimal getAdultPriceMyr() {
		return adultPriceMyr;
	}

	BigDecimal getChildPriceMyr() {
		return childPriceMyr;
	}

	void setPrices(BigDecimal adultPriceMyr, BigDecimal childPriceMyr) {
		this.adultPriceMyr = adultPriceMyr;
		this.childPriceMyr = childPriceMyr;
	}
}
