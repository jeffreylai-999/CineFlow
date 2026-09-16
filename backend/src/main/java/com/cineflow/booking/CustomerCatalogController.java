package com.cineflow.booking;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = "/api/movies", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Catalog")
public class CustomerCatalogController {

	private final Booking booking;

	CustomerCatalogController(Booking booking) {
		this.booking = booking;
	}

	@GetMapping
	@Operation(summary = "List Movies with current or future Showtimes grouped by Cinema date")
	public List<CustomerMovieResponse> listCatalog() {
		return booking.listCatalog();
	}
}
