package com.cineflow;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class MutableClock extends Clock {

	private Instant instant;
	private final ZoneId zone;

	public MutableClock(Instant instant) {
		this.instant = instant;
		this.zone = ZoneOffset.UTC;
	}

	public void set(Instant instant) {
		this.instant = instant;
	}

	@Override
	public ZoneId getZone() {
		return zone;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return Clock.fixed(instant, zone);
	}

	@Override
	public Instant instant() {
		return instant;
	}
}
