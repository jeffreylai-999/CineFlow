package com.cineflow.booking;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = "/api/showtimes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Booking")
public class CustomerShowtimeController {

	private final Booking booking;

	CustomerShowtimeController(Booking booking) {
		this.booking = booking;
	}

	@GetMapping("/{id}/seats")
	@Operation(summary = "Return customer Seat availability for a Showtime")
	public CustomerSeatMapResponse seats(@PathVariable long id) {
		return booking.showtimeSeats(id);
	}
}
