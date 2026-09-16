package com.cineflow.booking;

public record CheckoutResult(BookingConfirmationResponse confirmation, boolean replayed) {
}
