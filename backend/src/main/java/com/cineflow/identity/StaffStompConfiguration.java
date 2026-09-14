package com.cineflow.identity;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
class StaffStompConfiguration implements WebSocketMessageBrokerConfigurer {

	private final AccessTokens accessTokens;
	private final StaffAccountRepository staffAccounts;
	private final Clock clock;

	StaffStompConfiguration(AccessTokens accessTokens, StaffAccountRepository staffAccounts, Clock clock) {
		this.accessTokens = accessTokens;
		this.staffAccounts = staffAccounts;
		this.clock = clock;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws");
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic");
		registry.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if (accessor == null) {
					return message;
				}
				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
					accessor.setUser(authenticate(accessor));
				}
				else if (accessor.getCommand() != null && accessor.getCommand() != StompCommand.DISCONNECT) {
					requireActiveSession(accessor.getUser());
				}
				return message;
			}
		});
	}

	private Principal authenticate(StompHeaderAccessor accessor) {
		String authorization = accessor.getFirstNativeHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			throw IdentityException.unauthorized();
		}
		String token = authorization.substring("Bearer ".length()).trim();
		Jwt jwt = accessTokens.decode(token);
		requireActiveStaff(jwt);
		String role = jwt.getClaimAsString("role");
		List<SimpleGrantedAuthority> authorities = role == null
				? List.of()
				: List.of(new SimpleGrantedAuthority("ROLE_" + role));
		return new JwtAuthenticationToken(jwt, authorities);
	}

	private void requireActiveSession(Principal user) {
		if (!(user instanceof JwtAuthenticationToken authentication)) {
			throw IdentityException.unauthorized();
		}
		Jwt jwt = authentication.getToken();
		Instant expiresAt = jwt.getExpiresAt();
		if (expiresAt == null || !expiresAt.isAfter(clock.instant())) {
			throw IdentityException.unauthorized();
		}
		requireActiveStaff(jwt);
	}

	private void requireActiveStaff(Jwt jwt) {
		long staffId;
		try {
			staffId = Long.parseLong(jwt.getSubject());
		}
		catch (NumberFormatException exception) {
			throw IdentityException.unauthorized();
		}
		StaffAccountEntity staff = staffAccounts.findById(staffId).orElseThrow(IdentityException::unauthorized);
		if (!staff.isActive()) {
			throw IdentityException.unauthorized();
		}
	}
}
