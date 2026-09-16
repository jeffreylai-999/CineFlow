package com.cineflow.booking;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SeatHoldResponse(UUID holdId, long showtimeId, List<Long> seatIds, Instant serverTime, Instant expiresAt) {
}
