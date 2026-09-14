package com.cineflow.platform;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
class ProductionDatasourceGuard {

	static final int MAX_POOL_SIZE = 5;

	ProductionDatasourceGuard(
			@Value("${spring.datasource.url}") String jdbcUrl,
			@Value("${spring.datasource.hikari.maximum-pool-size}") int poolSize) {
		requirePersistentBackend(jdbcUrl, poolSize);
	}

	static void requirePersistentBackend(String jdbcUrl, int poolSize) {
		if (jdbcUrl == null || jdbcUrl.isBlank()) {
			throw new IllegalStateException("Production JDBC URL is required.");
		}

		String url = jdbcUrl.toLowerCase(Locale.ROOT);
		if (!url.contains("pooler.supabase.com")) {
			throw new IllegalStateException(
					"Production JDBC URL must use the Supavisor session pooler host (*.pooler.supabase.com).");
		}
		if (url.contains(":6543")) {
			throw new IllegalStateException(
					"Production JDBC URL must use session-mode port 5432, not transaction-mode port 6543.");
		}
		if (!url.contains(":5432")) {
			throw new IllegalStateException("Production JDBC URL must use session-mode port 5432.");
		}
		if (!(url.contains("sslmode=require") || url.contains("sslmode=verify-full"))) {
			throw new IllegalStateException("Production JDBC URL must set sslmode=require.");
		}
		if (poolSize < 1 || poolSize > MAX_POOL_SIZE) {
			throw new IllegalStateException(
					"Production Hikari pool size must be between 1 and 5 inclusive.");
		}
	}
}
