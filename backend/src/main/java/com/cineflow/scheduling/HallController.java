package com.cineflow.scheduling;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping(path = "/api/halls", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Scheduling")
public class HallController {

	private final Scheduling scheduling;

	HallController(Scheduling scheduling) {
		this.scheduling = scheduling;
	}

	@GetMapping
	@Operation(summary = "List Halls")
	public List<HallSummaryResponse> list() {
		return scheduling.listHalls();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Return a Hall and its Seat Map")
	public HallResponse get(@PathVariable long id) {
		return scheduling.getHall(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a Hall with a generated Seat Map")
	public HallResponse create(
			@Valid @RequestBody CreateHallRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return scheduling.createHall(Long.parseLong(jwt.getSubject()), request.name(), request.rowCount(),
				request.seatsPerRow());
	}

	@PatchMapping("/{hallId}/seats/{seatId}")
	@Operation(summary = "Enable or disable a Seat")
	public SeatResponse setSeatDisabled(
			@PathVariable long hallId,
			@PathVariable long seatId,
			@Valid @RequestBody SetSeatDisabledRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return scheduling.setSeatDisabled(
				Long.parseLong(jwt.getSubject()), hallId, seatId, request.disabled());
	}

	@PostMapping("/{id}/archive")
	@Operation(summary = "Archive a Hall so it cannot receive new Showtimes")
	public HallResponse archive(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
		return scheduling.archiveHall(Long.parseLong(jwt.getSubject()), id);
	}
}
