package com.cineflow.booking;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	private final BookingRateLimiter rateLimiter;
	private final ClientAddresses clientAddresses;

	CustomerShowtimeController(Booking booking, BookingRateLimiter rateLimiter, ClientAddresses clientAddresses) {
		this.booking = booking;
		this.rateLimiter = rateLimiter;
		this.clientAddresses = clientAddresses;
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
		rateLimiter.checkSeatHold(clientAddresses.of(httpRequest));
		return booking.createSeatHold(id, request.seatIds());
	}

	@PostMapping("/{id}/checkout")
	@Operation(summary = "Confirm an online Booking through simulated Payment")
	public ResponseEntity<BookingConfirmationResponse> checkout(
			@PathVariable long id,
			@Valid @RequestBody CheckoutRequest request,
			HttpServletRequest httpRequest) {
		rateLimiter.checkCheckout(clientAddresses.of(httpRequest));
		CheckoutResult result = booking.checkout(id, request);
		HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
		return ResponseEntity.status(status).body(result.confirmation());
	}
}
