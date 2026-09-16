package com.cineflow.booking;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.cineflow.platform.ClientAddresses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/showtimes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Booking")
public class CustomerShowtimeController {

	private final Booking booking;
	private final SeatHoldRateLimiter seatHoldRateLimiter;

	CustomerShowtimeController(Booking booking, SeatHoldRateLimiter seatHoldRateLimiter) {
		this.booking = booking;
		this.seatHoldRateLimiter = seatHoldRateLimiter;
	}

	@GetMapping("/{id}/seats")
	@Operation(summary = "Return customer Seat availability for a Showtime")
	public CustomerSeatMapResponse seats(@PathVariable long id) {
		return booking.showtimeSeats(id);
	}

	@PostMapping("/{id}/holds")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a ten-minute Seat Hold for a Showtime")
	public SeatHoldResponse createHold(
			@PathVariable long id,
			@Valid @RequestBody CreateSeatHoldRequest request,
			HttpServletRequest httpRequest) {
		seatHoldRateLimiter.check(ClientAddresses.of(httpRequest));
		return booking.createSeatHold(id, request.seatIds());
	}
}
