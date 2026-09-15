package com.cineflow.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateStaffAccountRequest(
		@NotBlank
		@Pattern(
				regexp = USERNAME_PATTERN,
				message = "Username must be 3-32 lowercase letters, digits, dots, underscores, or hyphens")
		String username,
		@NotBlank
		@Size(min = PasswordResetRequest.MIN_LENGTH, message = "Password must be at least 12 characters")
		String password) {

	static final String USERNAME_PATTERN = "^[a-z][a-z0-9._-]{2,31}$";
}
