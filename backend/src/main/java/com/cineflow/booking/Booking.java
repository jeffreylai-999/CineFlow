package com.cineflow.booking;

import java.util.List;

public interface Booking {

	List<CustomerMovieResponse> listCatalog();

	CustomerSeatMapResponse showtimeSeats(long showtimeId);
}
