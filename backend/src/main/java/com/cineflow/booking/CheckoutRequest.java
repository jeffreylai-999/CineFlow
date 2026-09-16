package com.cineflow.booking;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(
		@NotNull UUID holdId,
		@NotBlank @Email String email,
		@NotEmpty List<@NotNull @Valid CheckoutTicketRequest> tickets,
		@NotBlank String cardNumber,
		@NotBlank @Size(max = 100) String idempotencyKey) {
}
