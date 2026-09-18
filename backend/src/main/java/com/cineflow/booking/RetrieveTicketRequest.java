package com.cineflow.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RetrieveTicketRequest(
		@NotBlank @Email String email,
		@NotBlank @Size(max = 20) String bookingReference) {
}
