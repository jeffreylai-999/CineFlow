package com.cineflow.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
		@NotBlank @Size(min = MIN_LENGTH, message = "Password must be at least 12 characters") String password) {

	static final int MIN_LENGTH = 12;
}
