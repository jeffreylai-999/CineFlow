package com.cineflow.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

class ClientAddressesTest {

	@Test
	void usesTheFirstForwardedAddressWhenPresent() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.10, 10.0.0.1");
		when(request.getRemoteAddr()).thenReturn("10.0.0.1");

		assertThat(ClientAddresses.of(request)).isEqualTo("203.0.113.10");
	}

	@Test
	void fallsBackToTheServletPeerWhenNoForwardedHeaderIsPresent() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRemoteAddr()).thenReturn("127.0.0.1");

		assertThat(ClientAddresses.of(request)).isEqualTo("127.0.0.1");
	}
}
