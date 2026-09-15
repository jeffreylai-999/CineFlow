package com.cineflow.platform;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientAddresses {

	private ClientAddresses() {
	}

	public static String of(HttpServletRequest request) {
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
