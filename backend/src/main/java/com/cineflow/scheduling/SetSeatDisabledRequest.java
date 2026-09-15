package com.cineflow.scheduling;

import jakarta.validation.constraints.NotNull;

public record SetSeatDisabledRequest(@NotNull Boolean disabled) {
}
