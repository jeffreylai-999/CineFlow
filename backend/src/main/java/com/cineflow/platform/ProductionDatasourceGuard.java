package com.cineflow.platform;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
class ProductionDatasourceGuard {

	static final int MAX_POOL_SIZE = 5;

	private static final String POOLER_SUFFIX = ".pooler.supabase.com";
	private static final Set<String> TLS_SSL_MODES = Set.of("require", "verify-full");

	ProductionDatasourceGuard(
			@Value("${spring.datasource.url}") String jdbcUrl,
			@Value("${spring.datasource.hikari.maximum-pool-size}") int poolSize) {
		requirePersistentBackend(jdbcUrl, poolSize);
	}

	static void requirePersistentBackend(String jdbcUrl, int poolSize) {
		if (jdbcUrl == null || jdbcUrl.isBlank()) {
			throw new IllegalStateException("Production JDBC URL is required.");
		}

		URI uri = parseJdbcUri(jdbcUrl);
		if (!isSessionPoolerHost(uri.getHost())) {
			throw new IllegalStateException(
					"Production JDBC URL must use the Supavisor session pooler host (*.pooler.supabase.com).");
		}
		if (uri.getPort() == 6543) {
			throw new IllegalStateException(
					"Production JDBC URL must use session-mode port 5432, not transaction-mode port 6543.");
		}
		if (uri.getPort() != 5432) {
			throw new IllegalStateException("Production JDBC URL must use session-mode port 5432.");
		}
		if (!hasExactSslMode(uri.getQuery())) {
			throw new IllegalStateException("Production JDBC URL must set sslmode=require.");
		}
		if (poolSize < 1 || poolSize > MAX_POOL_SIZE) {
			throw new IllegalStateException(
					"Production Hikari pool size must be between 1 and 5 inclusive.");
		}
	}

	private static URI parseJdbcUri(String jdbcUrl) {
		String withoutPrefix = jdbcUrl.regionMatches(true, 0, "jdbc:", 0, 5)
				? jdbcUrl.substring(5)
				: jdbcUrl;
		try {
			URI uri = URI.create(withoutPrefix);
			if (uri.getHost() == null) {
				throw new IllegalStateException("Production JDBC URL is required.");
			}
			return uri;
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalStateException("Production JDBC URL is required.", ex);
		}
	}

	private static boolean isSessionPoolerHost(String host) {
		String hostname = host.toLowerCase(Locale.ROOT);
		return hostname.endsWith(POOLER_SUFFIX) && hostname.length() > POOLER_SUFFIX.length();
	}

	private static boolean hasExactSslMode(String query) {
		if (query == null || query.isBlank()) {
			return false;
		}
		for (String pair : query.split("&")) {
			int separator = pair.indexOf('=');
			if (separator <= 0) {
				continue;
			}
			String key = URLDecoder.decode(pair.substring(0, separator), StandardCharsets.UTF_8);
			String value = URLDecoder.decode(pair.substring(separator + 1), StandardCharsets.UTF_8);
			if ("sslmode".equalsIgnoreCase(key)) {
				return TLS_SSL_MODES.contains(value.toLowerCase(Locale.ROOT));
			}
		}
		return false;
	}
}
