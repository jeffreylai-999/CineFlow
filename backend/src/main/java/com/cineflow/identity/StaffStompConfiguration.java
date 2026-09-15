package com.cineflow.identity;

import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@Configuration
@EnableWebSocketMessageBroker
class StaffStompConfiguration implements WebSocketMessageBrokerConfigurer {

	private final AccessTokens accessTokens;
	private final StaffAccountRepository staffAccounts;
	private final Clock clock;
	private final ConcurrentHashMap<String, Principal> principals = new ConcurrentHashMap<>();

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
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
			@Override
			public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
				principals.remove(session.getId());
				super.afterConnectionClosed(session, closeStatus);
			}
		});
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
					Principal user = authenticate(accessor);
					accessor.setUser(user);
					if (accessor.getSessionId() != null) {
						principals.put(accessor.getSessionId(), user);
					}
				}
				else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
					if (accessor.getSessionId() != null) {
						principals.remove(accessor.getSessionId());
					}
				}
				else if (accessor.getCommand() != null) {
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
				if (accessor == null || !isOutboundPayload(accessor)) {
					return message;
				}
				Principal user = userOf(accessor);
				if (user == null) {
					return message;
				}
				try {
					requireActiveSession(user);
				}
				catch (IdentityException exception) {
					return null;
				}
				return message;
			}
		});
	}

	private static boolean isOutboundPayload(StompHeaderAccessor accessor) {
		if (accessor.getCommand() == StompCommand.MESSAGE) {
			return true;
		}
		return accessor.getCommand() == null && accessor.getMessageType() == SimpMessageType.MESSAGE;
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
		return sessionId == null ? null : principals.get(sessionId);
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
