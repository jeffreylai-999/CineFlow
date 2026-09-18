package com.cineflow.booking;

import java.math.BigDecimal;

public record CounterShowtimeResponse(
		long id,
		String movieTitle,
		String hallName,
		String startsAtCinemaTime,
		String timeZone,
		BigDecimal adultPriceMyr,
		BigDecimal childPriceMyr,
		boolean counterSalesOpen) {
}
