package com.cineflow.booking;

import java.math.BigDecimal;

public record BookedSeatResponse(long seatId, String label, TicketType ticketType, BigDecimal priceMyr) {
}
