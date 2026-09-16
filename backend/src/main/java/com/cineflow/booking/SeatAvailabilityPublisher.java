package com.cineflow.booking;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

	void publishAfterCommit(long showtimeId) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				publish(showtimeId);
			}
		});
	}
}
