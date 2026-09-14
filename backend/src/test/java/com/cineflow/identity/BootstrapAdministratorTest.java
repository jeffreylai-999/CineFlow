package com.cineflow.identity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class BootstrapAdministratorTest {

	@Test
	void rejectsBootstrapPasswordShorterThanTwelveCharacters() {
		StaffAccountRepository accounts = mock(StaffAccountRepository.class);
		when(accounts.countByRole(StaffRole.ADMINISTRATOR)).thenReturn(0L);
		BootstrapAdministrator bootstrap = new BootstrapAdministrator(
				accounts,
				new BootstrapAdministratorProperties("administrator", "short"),
				mock(PasswordEncoder.class),
				Clock.fixed(Instant.parse("2026-09-14T00:00:00Z"), ZoneOffset.UTC));

		assertThatThrownBy(() -> bootstrap.run(null))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("12");
	}
}
