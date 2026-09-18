package com.cineflow.booking;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cineflow.platform.ClientAddresses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/bookings", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Booking")
public class CustomerBookingController {

	private final Booking booking;
	private final BookingRateLimiter rateLimiter;
	private final ClientAddresses clientAddresses;

	CustomerBookingController(Booking booking, BookingRateLimiter rateLimiter, ClientAddresses clientAddresses) {
		this.booking = booking;
		this.rateLimiter = rateLimiter;
		this.clientAddresses = clientAddresses;
	}

	@PostMapping("/retrieve")
	@Operation(summary = "Retrieve a Booking and its Ticket with the email and Booking Reference")
	public BookingConfirmationResponse retrieve(
			@Valid @RequestBody RetrieveTicketRequest request,
			HttpServletRequest httpRequest) {
		rateLimiter.checkRetrieval(clientAddresses.of(httpRequest));
		return booking.retrieveTicket(request);
	}
}
