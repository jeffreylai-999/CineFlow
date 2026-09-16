package com.cineflow.booking;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

/**
 * Generates unpredictable Booking References from an unambiguous alphabet
 * (no 0/O/1/I/L) so customers can read them back without confusion.
 */
@Component
class BookingReferences {

	private static final char[] ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789".toCharArray();
	private static final int LENGTH = 10;

	private final SecureRandom random = new SecureRandom();

	String generate() {
		StringBuilder reference = new StringBuilder(LENGTH);
		for (int index = 0; index < LENGTH; index++) {
			reference.append(ALPHABET[random.nextInt(ALPHABET.length)]);
		}
		return reference.toString();
	}
}
