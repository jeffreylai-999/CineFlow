package com.cineflow.booking;

import jakarta.validation.constraints.NotNull;

public record CheckoutTicketRequest(@NotNull Long seatId, @NotNull TicketType ticketType) {
}
