package com.cineflow.identity;

import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
class StaffStompController {

	@MessageMapping("/staff/ping")
	@SendTo("/topic/staff/pong")
	Map<String, String> ping() {
		return Map.of("status", "ok");
	}
}
