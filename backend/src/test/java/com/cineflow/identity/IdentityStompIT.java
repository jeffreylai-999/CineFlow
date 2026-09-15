package com.cineflow.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.cineflow.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class IdentityStompIT {

	private static final String BOOKING_STAFF_HASH =
			"$2b$12$fjuTfnbHHQpXBdhjGl6NZ.j9dyjTLrCRr87JskFhkr1cUduAbhW6S";

	@LocalServerPort
	int port;

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	private WebSocketStompClient stompClient;

	@BeforeEach
	void setUp() {
		stompClient = new WebSocketStompClient(new StandardWebSocketClient());
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	}

	@Test
	void protectedStompConnectionRequiresAnAccessToken() throws Exception {
		String accessToken = loginAccessToken();
		StompSession session = connect(accessToken).get(5, TimeUnit.SECONDS);
		assertThat(session.isConnected()).isTrue();

		CompletableFuture<Map<String, Object>> pong = new CompletableFuture<>();
		session.subscribe("/topic/staff/pong", new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return Map.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				@SuppressWarnings("unchecked")
				Map<String, Object> body = (Map<String, Object>) payload;
				pong.complete(body);
			}
		});
		session.send("/app/staff/ping", Map.of());
		assertThat(pong.get(5, TimeUnit.SECONDS)).containsEntry("status", "ok");
		session.disconnect();
	}

	@Test
	void stompConnectWithoutAccessTokenIsRejected() {
		assertThatThrownBy(() -> connect(null).get(5, TimeUnit.SECONDS))
			.hasCauseInstanceOf(Exception.class);
	}

	@Test
	void stompConnectIsRejectedAfterDeactivation() throws Exception {
		StaffAccess staff = bookingStaffSession();
		deactivate(staff.id());
		assertThatThrownBy(() -> connect(staff.accessToken()).get(5, TimeUnit.SECONDS))
			.hasCauseInstanceOf(Exception.class);
	}

	@Test
	void protectedStompMessagesAreRejectedAfterDeactivation() throws Exception {
		StaffAccess staff = bookingStaffSession();
		StompSession session = connect(staff.accessToken()).get(5, TimeUnit.SECONDS);
		CompletableFuture<Map<String, Object>> pong = new CompletableFuture<>();
		session.subscribe("/topic/staff/pong", pongHandler(pong));
		deactivate(staff.id());
		session.send("/app/staff/ping", Map.of());
		assertThatThrownBy(() -> pong.get(2, TimeUnit.SECONDS))
			.isInstanceOf(TimeoutException.class);
		if (session.isConnected()) {
			session.disconnect();
		}
	}

	@Test
	void deactivatedSubscriberDoesNotReceiveTopicBroadcasts() throws Exception {
		StaffAccess staff = bookingStaffSession();
		StompSession inactive = connect(staff.accessToken()).get(5, TimeUnit.SECONDS);
		CompletableFuture<Map<String, Object>> inactivePong = new CompletableFuture<>();
		inactive.subscribe("/topic/staff/pong", pongHandler(inactivePong));
		deactivate(staff.id());

		StompSession active = connect(loginAccessToken()).get(5, TimeUnit.SECONDS);
		CompletableFuture<Map<String, Object>> activePong = new CompletableFuture<>();
		active.subscribe("/topic/staff/pong", pongHandler(activePong));
		active.send("/app/staff/ping", Map.of());

		assertThat(activePong.get(5, TimeUnit.SECONDS)).containsEntry("status", "ok");
		assertThatThrownBy(() -> inactivePong.get(2, TimeUnit.SECONDS))
			.isInstanceOf(TimeoutException.class);
		active.disconnect();
		if (inactive.isConnected()) {
			inactive.disconnect();
		}
	}

	private CompletableFuture<StompSession> connect(String accessToken) {
		StompHeaders connectHeaders = new StompHeaders();
		if (accessToken != null) {
			connectHeaders.add("Authorization", "Bearer " + accessToken);
		}
		return stompClient.connectAsync(
				"ws://localhost:" + port + "/ws",
				new WebSocketHttpHeaders(),
				connectHeaders,
				new StompSessionHandlerAdapter() {
				});
	}

	private String loginAccessToken() throws Exception {
		String body = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"administrator","password":"AdminPassw0rd!"}
						"""))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return JsonPath.read(body, "$.accessToken");
	}

	private StaffAccess bookingStaffSession() throws Exception {
		String username = "stomp." + UUID.randomUUID();
		long staffId = insertBookingStaff(username);
		String body = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"StaffPassw0rd!"}
						""".formatted(username)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return new StaffAccess(staffId, JsonPath.read(body, "$.accessToken"));
	}

	private void deactivate(long staffId) throws Exception {
		mockMvc.perform(post("/api/staff/accounts/" + staffId + "/deactivate")
				.header("Authorization", "Bearer " + loginAccessToken()))
			.andExpect(status().isNoContent());
	}

	private long insertBookingStaff(String username) {
		jdbcTemplate.update(
				"""
						insert into cineflow.staff_accounts (username, password_hash, role, active, created_at)
						values (?, ?, 'BOOKING_STAFF', true, now())
						""",
				username,
				BOOKING_STAFF_HASH);
		return jdbcTemplate.queryForObject(
				"select id from cineflow.staff_accounts where username = ?",
				Long.class,
				username);
	}

	private static StompFrameHandler pongHandler(CompletableFuture<Map<String, Object>> pong) {
		return new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return Map.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				@SuppressWarnings("unchecked")
				Map<String, Object> body = (Map<String, Object>) payload;
				pong.complete(body);
			}
		};
	}

	private record StaffAccess(long id, String accessToken) {
	}
}
