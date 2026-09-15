package com.cineflow.identity;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.cineflow.platform.CorrelationIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
class SecurityConfiguration {

	private static final ObjectMapper PROBLEM_MAPPER = new ObjectMapper();

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		AuthenticationEntryPoint entryPoint = problemEntryPoint();
		AccessDeniedHandler accessDeniedHandler = problemAccessDeniedHandler();
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh", "/api/auth/logout")
				.permitAll()
				.requestMatchers(HttpMethod.GET, "/api/movies")
				.permitAll()
				.requestMatchers("/api/staff/accounts", "/api/staff/accounts/**")
				.hasRole("ADMINISTRATOR")
				.requestMatchers("/api/**")
				.authenticated()
				.requestMatchers("/actuator/health", "/actuator/health/**")
				.permitAll()
				.requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
				.permitAll()
				.requestMatchers("/ws")
				.permitAll()
				.anyRequest()
				.permitAll())
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
				.authenticationEntryPoint(entryPoint)
				.accessDeniedHandler(accessDeniedHandler))
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(entryPoint)
				.accessDeniedHandler(accessDeniedHandler));
		return http.build();
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			String role = jwt.getClaimAsString("role");
			if (role == null || role.isBlank()) {
				return List.of();
			}
			return List.of(new SimpleGrantedAuthority("ROLE_" + role));
		});
		return converter;
	}

	private static AuthenticationEntryPoint problemEntryPoint() {
		return (HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) ->
			writeProblem(response, HttpStatus.UNAUTHORIZED, "auth.unauthorized", "Unauthorized");
	}

	private static AccessDeniedHandler problemAccessDeniedHandler() {
		return (request, response, exception) ->
			writeProblem(response, HttpStatus.FORBIDDEN, "auth.forbidden", "Forbidden");
	}

	private static void writeProblem(
			HttpServletResponse response,
			HttpStatus status,
			String code,
			String title) throws IOException {
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		LinkedHashMap<String, Object> body = new LinkedHashMap<>();
		body.put("type", "about:blank");
		body.put("title", title);
		body.put("status", status.value());
		body.put("code", code);
		if (correlationId != null && !correlationId.isBlank()) {
			body.put("correlationId", correlationId);
		}
		PROBLEM_MAPPER.writeValue(response.getOutputStream(), body);
	}
}
