package com.cineflow.booking;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping(path = "/api/staff/showtimes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Booking")
public class StaffBookingController {

	private final Booking booking;
	private final BookingRateLimiter rateLimiter;
	private final ClientAddresses clientAddresses;

	StaffBookingController(Booking booking, BookingRateLimiter rateLimiter, ClientAddresses clientAddresses) {
		this.booking = booking;
		this.rateLimiter = rateLimiter;
		this.clientAddresses = clientAddresses;
	}

	@GetMapping
	@Operation(summary = "List Showtimes open for counter sales")
	public List<CounterShowtimeResponse> counterShowtimes() {
		return booking.listCounterSaleShowtimes();
	}

	@GetMapping("/{id}/seats")
	@Operation(summary = "Return the Staff Seat Map distinguishing Seat Holds, Bookings, and Disabled Seats")
	public StaffSeatMapResponse seats(@PathVariable long id) {
		return booking.staffSeatMap(id);
	}

	@PostMapping("/{id}/holds")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a ten-minute Seat Hold for a counter sale")
	public SeatHoldResponse createHold(
			@PathVariable long id,
			@Valid @RequestBody CreateSeatHoldRequest request,
			HttpServletRequest httpRequest) {
		rateLimiter.checkSeatHold(clientAddresses.of(httpRequest));
		return booking.createCounterSeatHold(id, request.seatIds());
	}

	@PostMapping("/{id}/confirm")
	@Operation(summary = "Confirm a Staff-Assisted Booking with a received Cash or Card Payment")
	public ResponseEntity<BookingConfirmationResponse> confirm(
			@PathVariable long id,
			@Valid @RequestBody CounterSaleRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		CheckoutResult result = booking.confirmCounterSale(Long.parseLong(jwt.getSubject()), id, request);
		HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
		return ResponseEntity.status(status).body(result.confirmation());
	}
}
