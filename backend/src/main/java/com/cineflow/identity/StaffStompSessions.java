package com.cineflow.identity;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

@Component
class StaffStompSessions {

	private final ConcurrentHashMap<String, Principal> principals = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, WebSocketSession> sockets = new ConcurrentHashMap<>();

	WebSocketHandler decorate(WebSocketHandler handler) {
		return new WebSocketHandlerDecorator(handler) {
			@Override
			public void afterConnectionEstablished(WebSocketSession session) throws Exception {
				sockets.put(session.getId(), session);
				super.afterConnectionEstablished(session);
			}

			@Override
			public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
				forget(session.getId());
				super.afterConnectionClosed(session, closeStatus);
			}
		};
	}

	void remember(Message<?> message, Principal user) {
		String sessionId = sessionId(message);
		if (sessionId != null && user != null) {
			principals.put(sessionId, user);
		}
	}

	void forget(Message<?> message) {
		String sessionId = sessionId(message);
		if (sessionId != null) {
			forget(sessionId);
		}
	}

	Principal principal(Message<?> message) {
		String sessionId = sessionId(message);
		return sessionId == null ? null : principals.get(sessionId);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	void closeDeactivated(StaffDeactivated event) {
		String subject = Long.toString(event.staffId());
		for (String sessionId : new ArrayList<>(principals.keySet())) {
			Principal user = principals.get(sessionId);
			if (user == null || !subject.equals(user.getName())) {
				continue;
			}
			WebSocketSession socket = sockets.get(sessionId);
			if (socket == null || !socket.isOpen()) {
				continue;
			}
			try {
				socket.close(CloseStatus.POLICY_VIOLATION);
			}
			catch (IOException ignored) {
				// The outbound interceptor still drops topic frames if close races the broker.
			}
		}
	}

	private void forget(String sessionId) {
		principals.remove(sessionId);
		sockets.remove(sessionId);
	}

	private static String sessionId(Message<?> message) {
		return SimpMessageHeaderAccessor.getSessionId(message.getHeaders());
	}
}
