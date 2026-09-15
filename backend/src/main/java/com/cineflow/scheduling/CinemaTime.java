package com.cineflow.scheduling;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

final class CinemaTime {

	static final ZoneId ZONE = ZoneId.of("Asia/Kuala_Lumpur");
	static final int CLEANING_BUFFER_MINUTES = 15;
	static final Duration CLEANING_BUFFER = Duration.ofMinutes(CLEANING_BUFFER_MINUTES);

	private CinemaTime() {
	}

	static Instant toInstant(String startsAtLocal, String timeZone) {
		if (startsAtLocal == null || startsAtLocal.isBlank() || timeZone == null || timeZone.isBlank()) {
			throw SchedulingException.invalidCinemaTime();
		}
		if (!ZONE.getId().equals(timeZone)) {
			throw SchedulingException.invalidCinemaTime();
		}
		try {
			return LocalDateTime.parse(startsAtLocal, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZONE).toInstant();
		}
		catch (DateTimeParseException exception) {
			throw SchedulingException.invalidCinemaTime();
		}
	}

	static Instant occupancyEnd(Instant startsAt, int runtimeMinutes) {
		return startsAt.plus(Duration.ofMinutes(runtimeMinutes)).plus(CLEANING_BUFFER);
	}

	static String formatLocal(Instant instant) {
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(instant.atZone(ZONE));
	}
}
