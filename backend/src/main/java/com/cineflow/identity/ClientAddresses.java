package com.cineflow.identity;

import jakarta.servlet.http.HttpServletRequest;

final class ClientAddresses {

	private ClientAddresses() {
	}

	static String of(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			int comma = forwarded.indexOf(',');
			String first = comma < 0 ? forwarded.trim() : forwarded.substring(0, comma).trim();
			if (!first.isEmpty()) {
				return first;
			}
		}
		String remote = request.getRemoteAddr();
		return remote == null || remote.isBlank() ? "unknown" : remote;
	}
}
