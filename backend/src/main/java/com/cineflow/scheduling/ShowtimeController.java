package com.cineflow.scheduling;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/showtimes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Scheduling")
public class ShowtimeController {

	private final Scheduling scheduling;

	ShowtimeController(Scheduling scheduling) {
		this.scheduling = scheduling;
	}

	@GetMapping
	@Operation(summary = "List Showtimes in Cinema Time")
	public List<ShowtimeResponse> list() {
		return scheduling.listShowtimes();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Schedule a Showtime with Adult and Child Ticket Prices")
	public ShowtimeResponse create(
			@Valid @RequestBody CreateShowtimeRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return scheduling.createShowtime(
				Long.parseLong(jwt.getSubject()),
				request.movieId(),
				request.hallId(),
				request.startsAtLocal(),
				request.timeZone(),
				request.adultPriceMyr(),
				request.childPriceMyr());
	}

	@PatchMapping("/{id}")
	@Operation(summary = "Update Showtime Ticket Prices")
	public ShowtimeResponse updatePrices(
			@PathVariable long id,
			@Valid @RequestBody UpdateShowtimePricesRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return scheduling.updateShowtimePrices(
				Long.parseLong(jwt.getSubject()),
				id,
				request.adultPriceMyr(),
				request.childPriceMyr());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Remove an unused future Showtime")
	public void remove(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
		scheduling.removeShowtime(Long.parseLong(jwt.getSubject()), id);
	}
}
