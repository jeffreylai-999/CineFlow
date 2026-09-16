package com.cineflow.booking;

import java.math.BigDecimal;

public record CustomerShowtimeSummary(
		long id,
		String hallName,
		String startsAtCinemaTime,
		String timeZone,
		BigDecimal adultPriceMyr,
		BigDecimal childPriceMyr,
		boolean checkoutOpen) {
}
