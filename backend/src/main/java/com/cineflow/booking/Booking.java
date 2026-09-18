package com.cineflow.booking;

import java.util.List;

public interface Booking {

	List<CustomerMovieResponse> listCatalog();

	CustomerSeatMapResponse showtimeSeats(long showtimeId);

	SeatHoldResponse createSeatHold(long showtimeId, List<Long> seatIds);

	CheckoutResult checkout(long showtimeId, CheckoutRequest request);

	BookingConfirmationResponse retrieveTicket(RetrieveTicketRequest request);
}
