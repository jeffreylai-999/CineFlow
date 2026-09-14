package com.cineflow.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

	@LocalServerPort
	int port;

	@Autowired
	MockMvc mockMvc;

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
}
