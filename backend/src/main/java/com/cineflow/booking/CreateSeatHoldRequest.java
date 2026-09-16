package com.cineflow.booking;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateSeatHoldRequest(@NotEmpty List<@NotNull Long> seatIds) {
}
