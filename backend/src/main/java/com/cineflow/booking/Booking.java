package com.cineflow.booking;

import java.util.List;

public interface Booking {

	List<CustomerMovieResponse> listCatalog();

	CustomerSeatMapResponse showtimeSeats(long showtimeId);

	SeatHoldResponse createSeatHold(long showtimeId, List<Long> seatIds);

	CheckoutResult checkout(long showtimeId, CheckoutRequest request);

	BookingConfirmationResponse retrieveTicket(RetrieveTicketRequest request);

	List<CounterShowtimeResponse> listCounterSaleShowtimes();

	StaffSeatMapResponse staffSeatMap(long showtimeId);

	SeatHoldResponse createCounterSeatHold(long showtimeId, List<Long> seatIds);

	CheckoutResult confirmCounterSale(long actorStaffId, long showtimeId, CounterSaleRequest request);
}
