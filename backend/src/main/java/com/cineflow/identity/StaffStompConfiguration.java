package com.cineflow.identity;

import java.security.Principal;
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

	StaffStompConfiguration(AccessTokens accessTokens) {
		this.accessTokens = accessTokens;
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
				else if (accessor.getUser() == null && accessor.getCommand() != null
						&& accessor.getCommand() != StompCommand.DISCONNECT) {
					throw IdentityException.unauthorized();
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
		String role = jwt.getClaimAsString("role");
		List<SimpleGrantedAuthority> authorities = role == null
				? List.of()
				: List.of(new SimpleGrantedAuthority("ROLE_" + role));
		return new JwtAuthenticationToken(jwt, authorities);
	}
}
