package com.cineflow.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

class ClientAddressesTest {

	@Test
	void usesTheRightmostAddressAtTheTrustedProxyDepth() {
		ClientAddresses addresses = new ClientAddresses(new HttpProperties(1));
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.10, 198.51.100.20");
		when(request.getRemoteAddr()).thenReturn("10.0.0.1");

		assertThat(addresses.of(request)).isEqualTo("198.51.100.20");
	}

	@Test
	void ignoresCallerControlledLeftmostHopsWhenATrustedProxyAppends() {
		ClientAddresses addresses = new ClientAddresses(new HttpProperties(1));
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("X-Forwarded-For"))
				.thenReturn("203.0.113.10, 198.51.100.77, 203.0.113.50");
		when(request.getRemoteAddr()).thenReturn("10.0.0.1");

		assertThat(addresses.of(request)).isEqualTo("203.0.113.50");
	}

	@Test
	void honorsAConfiguredProxyDepthGreaterThanOne() {
		ClientAddresses addresses = new ClientAddresses(new HttpProperties(2));
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("X-Forwarded-For"))
				.thenReturn("203.0.113.10, 198.51.100.20, 192.0.2.1");
		when(request.getRemoteAddr()).thenReturn("10.0.0.1");

		assertThat(addresses.of(request)).isEqualTo("198.51.100.20");
	}

	@Test
	void fallsBackToTheServletPeerWhenNoForwardedHeaderIsPresent() {
		ClientAddresses addresses = new ClientAddresses(new HttpProperties(1));
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRemoteAddr()).thenReturn("127.0.0.1");

		assertThat(addresses.of(request)).isEqualTo("127.0.0.1");
	}

	@Test
	void fallsBackToTheServletPeerWhenTheChainIsShorterThanTheTrustedDepth() {
		ClientAddresses addresses = new ClientAddresses(new HttpProperties(2));
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.10");
		when(request.getRemoteAddr()).thenReturn("10.0.0.1");

		assertThat(addresses.of(request)).isEqualTo("10.0.0.1");
	}
}
