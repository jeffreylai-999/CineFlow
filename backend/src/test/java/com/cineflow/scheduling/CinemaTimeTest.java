package com.cineflow.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

class CinemaTimeTest {

	@Test
	void zonelessLocalDateTimeUsesCinemaTimeEvenWhenTheJvmZoneIsDifferent() {
		TimeZone original = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		try {
			Instant startsAt = CinemaTime.toInstant("2026-09-20T19:30", "Asia/Kuala_Lumpur");
			assertThat(startsAt).isEqualTo(Instant.parse("2026-09-20T11:30:00Z"));
			assertThat(CinemaTime.occupancyEnd(startsAt, 90))
				.isEqualTo(Instant.parse("2026-09-20T13:15:00Z"));
		}
		finally {
			TimeZone.setDefault(original);
		}
	}

	@Test
	void aDifferentZoneIdentifierIsRejected() {
		assertThatThrownBy(() -> CinemaTime.toInstant("2026-09-20T19:30", "America/New_York"))
			.isInstanceOf(SchedulingException.class)
			.extracting(error -> ((SchedulingException) error).code())
			.isEqualTo("scheduling.invalid_cinema_time");
	}
}
