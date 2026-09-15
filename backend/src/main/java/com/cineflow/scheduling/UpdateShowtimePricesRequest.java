package com.cineflow.scheduling;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record UpdateShowtimePricesRequest(
		@NotNull @DecimalMin("0.01") @Digits(integer = 6, fraction = 2) BigDecimal adultPriceMyr,
		@NotNull @DecimalMin("0.01") @Digits(integer = 6, fraction = 2) BigDecimal childPriceMyr) {
}
