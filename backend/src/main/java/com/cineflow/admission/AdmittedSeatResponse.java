package com.cineflow.admission;

import com.cineflow.booking.TicketType;

public record AdmittedSeatResponse(String label, TicketType ticketType) {
}
