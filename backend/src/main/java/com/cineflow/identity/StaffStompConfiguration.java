package com.cineflow.identity;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
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
	private final ObjectProvider<SimpUserRegistry> userRegistry;

	StaffStompConfiguration(
			AccessTokens accessTokens,
			StaffAccountRepository staffAccounts,
			Clock clock,
			ObjectProvider<SimpUserRegistry> userRegistry) {
		this.accessTokens = accessTokens;
		this.staffAccounts = staffAccounts;
		this.clock = clock;
		this.userRegistry = userRegistry;
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

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if (accessor == null || accessor.getCommand() != StompCommand.MESSAGE) {
					return message;
				}
				try {
					requireActiveSession(userOf(accessor));
				}
				catch (IdentityException exception) {
					return null;
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
		requireActiveStaff(jwt.getSubject());
		String role = jwt.getClaimAsString("role");
		List<SimpleGrantedAuthority> authorities = role == null
				? List.of()
				: List.of(new SimpleGrantedAuthority("ROLE_" + role));
		return new JwtAuthenticationToken(jwt, authorities);
	}

	private Principal userOf(StompHeaderAccessor accessor) {
		Principal user = accessor.getUser();
		if (user != null) {
			return user;
		}
		String sessionId = accessor.getSessionId();
		SimpUserRegistry registry = userRegistry.getIfAvailable();
		if (sessionId == null || registry == null) {
			return null;
		}
		for (SimpUser simpUser : registry.getUsers()) {
			if (simpUser.getSession(sessionId) != null) {
				return simpUser::getName;
			}
		}
		return null;
	}

	private void requireActiveSession(Principal user) {
		if (user instanceof JwtAuthenticationToken authentication) {
			Jwt jwt = authentication.getToken();
			Instant expiresAt = jwt.getExpiresAt();
			if (expiresAt == null || !expiresAt.isAfter(clock.instant())) {
				throw IdentityException.unauthorized();
			}
			requireActiveStaff(jwt.getSubject());
			return;
		}
		if (user == null) {
			throw IdentityException.unauthorized();
		}
		requireActiveStaff(user.getName());
	}

	private void requireActiveStaff(String subject) {
		long staffId;
		try {
			staffId = Long.parseLong(subject);
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
