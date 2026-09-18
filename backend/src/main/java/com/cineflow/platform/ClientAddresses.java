package com.cineflow.platform;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class ClientAddresses {

	private final int trustedProxyDepth;

	ClientAddresses(HttpProperties httpProperties) {
		this.trustedProxyDepth = httpProperties.trustedProxyDepth();
	}

	public String of(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			String[] hops = forwarded.split(",");
			if (hops.length >= trustedProxyDepth) {
				String candidate = hops[hops.length - trustedProxyDepth].trim();
				if (!candidate.isEmpty()) {
					return candidate;
				}
			}
		}
		String remote = request.getRemoteAddr();
		return remote == null || remote.isBlank() ? "unknown" : remote;
	}
}
