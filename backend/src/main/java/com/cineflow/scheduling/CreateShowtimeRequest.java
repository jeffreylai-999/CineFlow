package com.cineflow.scheduling;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateShowtimeRequest(
		@Positive long movieId,
		@Positive long hallId,
		@NotBlank String startsAtLocal,
		@NotBlank String timeZone,
		@NotNull @DecimalMin("0.01") @Digits(integer = 6, fraction = 2) BigDecimal adultPriceMyr,
		@NotNull @DecimalMin("0.01") @Digits(integer = 6, fraction = 2) BigDecimal childPriceMyr) {
}
