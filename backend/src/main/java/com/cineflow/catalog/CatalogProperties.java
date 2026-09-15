package com.cineflow.catalog;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cineflow.catalog")
public record CatalogProperties(int searchRateLimit, Duration rateLimitWindow) {
}
