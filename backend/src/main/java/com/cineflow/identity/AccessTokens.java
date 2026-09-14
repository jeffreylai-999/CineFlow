package com.cineflow.identity;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
class AccessTokens {

	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;
	private final AuthProperties authProperties;
	private final Clock clock;

	AccessTokens(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, AuthProperties authProperties, Clock clock) {
		this.jwtEncoder = jwtEncoder;
		this.jwtDecoder = jwtDecoder;
		this.authProperties = authProperties;
		this.clock = clock;
	}

	String issue(StaffAccountEntity staff) {
		Instant now = clock.instant();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.id(UUID.randomUUID().toString())
			.subject(Long.toString(staff.getId()))
			.issuedAt(now)
			.expiresAt(now.plus(authProperties.accessTokenTtl()))
			.claim("role", staff.getRole().name())
			.claim("username", staff.getUsername())
			.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				claims))
			.getTokenValue();
	}

	Jwt decode(String accessToken) {
		try {
			return jwtDecoder.decode(accessToken);
		}
		catch (JwtException exception) {
			throw IdentityException.unauthorized();
		}
	}

}
