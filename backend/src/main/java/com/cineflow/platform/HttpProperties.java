package com.cineflow.platform;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cineflow.http")
public record HttpProperties(int trustedProxyDepth) {

	public HttpProperties {
		if (trustedProxyDepth < 1) {
			throw new IllegalArgumentException("cineflow.http.trusted-proxy-depth must be at least 1");
		}
	}
}
