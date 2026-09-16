package com.cineflow.booking;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

/**
 * Issues the opaque Admission token carried by a Ticket's QR code. CineFlow
 * stores only the SHA-256 hash; the raw token is shown to the customer once.
 */
@Component
class AdmissionTokens {

	private static final int TOKEN_BYTES = 32;

	private final SecureRandom random = new SecureRandom();

	String newToken() {
		byte[] bytes = new byte[TOKEN_BYTES];
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	String hash(String token) {
		return Sha256.hash(token);
	}
}
