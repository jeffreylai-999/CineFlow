package com.cineflow.booking;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
class SeatAvailabilityPublisher {

	private final SimpMessagingTemplate messaging;

	SeatAvailabilityPublisher(SimpMessagingTemplate messaging) {
		this.messaging = messaging;
	}

	void publish(long showtimeId) {
		messaging.convertAndSend(
				"/topic/showtimes/" + showtimeId + "/availability",
				new SeatAvailabilityInvalidation(showtimeId));
	}
}
