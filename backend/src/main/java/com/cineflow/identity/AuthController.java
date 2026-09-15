package com.cineflow.identity;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Identity")
public class AuthController {

	private final Identity identity;
	private final AuthProperties authProperties;
	private final AuthRateLimiter rateLimiter;

	AuthController(Identity identity, AuthProperties authProperties, AuthRateLimiter rateLimiter) {
		this.identity = identity;
		this.authProperties = authProperties;
		this.rateLimiter = rateLimiter;
	}

	@PostMapping("/login")
	@Operation(summary = "Sign in a Staff account")
	public ResponseEntity<StaffSessionResponse> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest) {
		rateLimiter.checkLogin(clientKey(httpRequest, request.username()));
		StaffSession session = identity.login(request.username(), request.password());
		return withRefreshCookie(session);
	}

	@PostMapping("/refresh")
	@Operation(summary = "Rotate the refresh-token family and issue a new access token")
	public ResponseEntity<StaffSessionResponse> refresh(HttpServletRequest httpRequest) {
		rateLimiter.checkRefresh(ClientAddresses.of(httpRequest));
		StaffSession session = identity.refresh(readRefreshCookie(httpRequest));
		return withRefreshCookie(session);
	}

	@PostMapping("/logout")
	@Operation(summary = "Revoke the current refresh-token family")
	public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
		identity.logout(readRefreshCookie(httpRequest));
		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
			.build();
	}

	private ResponseEntity<StaffSessionResponse> withRefreshCookie(StaffSession session) {
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, refreshCookie(session.refreshToken()).toString())
			.body(StaffSessionResponse.from(session));
	}

	private ResponseCookie refreshCookie(String token) {
		AuthProperties.Cookie cookie = authProperties.cookie();
		return ResponseCookie.from(cookie.name(), token)
			.httpOnly(true)
			.secure(cookie.secure())
			.path(cookie.path())
			.maxAge(authProperties.refreshTokenTtl())
			.sameSite(cookie.sameSite())
			.build();
	}

	private ResponseCookie clearRefreshCookie() {
		AuthProperties.Cookie cookie = authProperties.cookie();
		return ResponseCookie.from(cookie.name(), "")
			.httpOnly(true)
			.secure(cookie.secure())
			.path(cookie.path())
			.maxAge(0)
			.sameSite(cookie.sameSite())
			.build();
	}

	private String readRefreshCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		String name = authProperties.cookie().name();
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private static String clientKey(HttpServletRequest request, String username) {
		String value = username == null ? "" : username.toLowerCase();
		return ClientAddresses.of(request) + ":" + value;
	}
}
