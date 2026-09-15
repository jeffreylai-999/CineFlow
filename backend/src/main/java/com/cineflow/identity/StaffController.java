package com.cineflow.identity;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/staff")
@Tag(name = "Identity")
public class StaffController {

	private final Identity identity;

	StaffController(Identity identity) {
		this.identity = identity;
	}

	@GetMapping("/me")
	@Operation(summary = "Return the authenticated Staff profile")
	public StaffProfile me(@AuthenticationPrincipal Jwt jwt) {
		return identity.me(Long.parseLong(jwt.getSubject()));
	}

	@GetMapping("/accounts")
	@Operation(summary = "List Staff accounts")
	public List<StaffAccountSummary> list(@AuthenticationPrincipal Jwt jwt) {
		return identity.listStaffAccounts(Long.parseLong(jwt.getSubject()));
	}

	@PostMapping("/accounts")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a Booking Staff account")
	public StaffAccountSummary create(
			@Valid @RequestBody CreateStaffAccountRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		return identity.createBookingStaff(Long.parseLong(jwt.getSubject()), request.username(), request.password());
	}

	@PostMapping("/accounts/{id}/deactivate")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Deactivate a Booking Staff account and revoke its token families")
	public void deactivate(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
		identity.deactivate(Long.parseLong(jwt.getSubject()), id);
	}

	@PostMapping("/accounts/{id}/password-reset")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Reset a Booking Staff password and revoke its token families")
	public void resetPassword(
			@PathVariable long id,
			@Valid @RequestBody PasswordResetRequest request,
			@AuthenticationPrincipal Jwt jwt) {
		identity.resetPassword(Long.parseLong(jwt.getSubject()), id, request.password());
	}
}
