package com.cineflow.booking;

import org.springframework.stereotype.Component;

/**
 * Deterministic stand-in for a card acquirer. The card number decides the
 * outcome of the simulated Payment and is never stored: 4242424242424242 (or
 * any Luhn-valid number) succeeds, 4000000000000002 is always declined.
 */
@Component
class SimulatedCardPayments {

	static final String DECLINED_CARD_NUMBER = "4000000000000002";

	boolean approve(String cardNumber) {
		String normalized = cardNumber.replace(" ", "").replace("-", "");
		if (normalized.length() < 12 || normalized.length() > 19 || !normalized.chars().allMatch(Character::isDigit)) {
			return false;
		}
		if (DECLINED_CARD_NUMBER.equals(normalized)) {
			return false;
		}
		return passesLuhn(normalized);
	}

	private static boolean passesLuhn(String digits) {
		int sum = 0;
		boolean doubleDigit = false;
		for (int index = digits.length() - 1; index >= 0; index--) {
			int digit = digits.charAt(index) - '0';
			if (doubleDigit) {
				digit *= 2;
				if (digit > 9) {
					digit -= 9;
				}
			}
			sum += digit;
			doubleDigit = !doubleDigit;
		}
		return sum % 10 == 0;
	}
}
