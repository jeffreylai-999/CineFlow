package com.cineflow.identity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cineflow.audit.Audit;

class IdentityServiceLoginTest {

	private static final String DUMMY_HASH = "$2a$12$dummy-password-hash-value.............";
	private static final String STORED_HASH = "$2a$12$stored-password-hash-value............";

	private StaffAccountRepository staffAccounts;
	private PasswordEncoder passwordEncoder;
	private IdentityService identity;

	@BeforeEach
	void setUp() {
		staffAccounts = mock(StaffAccountRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		when(passwordEncoder.encode(anyString())).thenReturn(DUMMY_HASH);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
		identity = new IdentityService(
				staffAccounts,
				mock(RefreshTokenFamilyRepository.class),
				mock(RefreshTokenRepository.class),
				passwordEncoder,
				mock(AccessTokens.class),
				new AuthProperties(
						Duration.ofMinutes(15),
						Duration.ofHours(8),
						new AuthProperties.Cookie("cineflow_refresh", false, "Lax", "/api/auth"),
						10,
						30,
						Duration.ofMinutes(1),
						new AuthProperties.Jwt("local-dev-only-jwt-secret-key-32b")),
				mock(Audit.class),
				Clock.fixed(Instant.parse("2026-09-15T00:00:00Z"), ZoneOffset.UTC),
				mock(ApplicationEventPublisher.class));
	}

	@Test
	void verifiesADummyHashWhenTheUsernameIsUnknown() {
		when(staffAccounts.findByUsernameForUpdate("nobody")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> identity.login("nobody", "guess"))
				.isInstanceOf(IdentityException.class);

		verify(passwordEncoder).matches("guess", DUMMY_HASH);
	}

	@Test
	void verifiesADummyHashWhenTheAccountIsInactive() {
		StaffAccountEntity staff = new StaffAccountEntity(
				"alice",
				STORED_HASH,
				StaffRole.BOOKING_STAFF,
				Instant.parse("2026-09-15T00:00:00Z"));
		staff.deactivate();
		when(staffAccounts.findByUsernameForUpdate("alice")).thenReturn(Optional.of(staff));

		assertThatThrownBy(() -> identity.login("alice", "guess"))
				.isInstanceOf(IdentityException.class);

		verify(passwordEncoder).matches("guess", DUMMY_HASH);
		verify(passwordEncoder, never()).matches(eq("guess"), eq(STORED_HASH));
	}
}
