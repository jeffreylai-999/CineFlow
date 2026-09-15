package com.cineflow.identity;

import java.time.Duration;

public record StaffSession(
		String accessToken,
		Duration accessTokenTtl,
		String refreshToken,
		Duration refreshTokenTtl,
		StaffProfile staff) {
}
