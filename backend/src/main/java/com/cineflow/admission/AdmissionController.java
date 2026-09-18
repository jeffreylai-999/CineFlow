package com.cineflow.admission;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = "/api/staff/admissions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Admission")
public class AdmissionController {

	private final Admission admission;

	AdmissionController(Admission admission) {
		this.admission = admission;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Admit a whole Booking once by Ticket QR Admission token or Booking Reference")
	public AdmissionResponse admit(
			@RequestBody AdmitBookingRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return admission.admit(Long.parseLong(jwt.getSubject()), request);
	}
}
