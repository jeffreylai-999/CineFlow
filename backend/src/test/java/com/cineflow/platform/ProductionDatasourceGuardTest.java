package com.cineflow.platform;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ProductionDatasourceGuardTest {

	private static final String SESSION_POOLER =
			"jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require";

	@Test
	void acceptsSessionPoolerUrlWithTlsAndASmallPool() {
		assertThatCode(() -> ProductionDatasourceGuard.requirePersistentBackend(SESSION_POOLER, 3))
			.doesNotThrowAnyException();
	}

	@Test
	void rejectsTransactionPoolerPort() {
		assertThatThrownBy(() -> ProductionDatasourceGuard.requirePersistentBackend(
				"jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require",
				3))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("5432");
	}

	@Test
	void rejectsUrlWithoutRequiredTls() {
		assertThatThrownBy(() -> ProductionDatasourceGuard.requirePersistentBackend(
				"jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres",
				3))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("sslmode=require");
	}

	@Test
	void rejectsDirectSupabaseHost() {
		assertThatThrownBy(() -> ProductionDatasourceGuard.requirePersistentBackend(
				"jdbc:postgresql://db.abcdefghijklmnop.supabase.co:5432/postgres?sslmode=require",
				3))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("pooler.supabase.com");
	}

	@Test
	void rejectsAPoolAboveTheFreeTierBudget() {
		assertThatThrownBy(() -> ProductionDatasourceGuard.requirePersistentBackend(SESSION_POOLER, 6))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("5");
	}
}
