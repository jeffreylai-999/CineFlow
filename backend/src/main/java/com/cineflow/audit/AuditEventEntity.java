package com.cineflow.audit;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_events", schema = "cineflow")
class AuditEventEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "actor_staff_id")
	private Long actorStaffId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AuditAction action;

	@Column(name = "subject_type")
	private String subjectType;

	@Column(name = "subject_id")
	private String subjectId;

	@Column(name = "correlation_id")
	private String correlationId;

	protected AuditEventEntity() {
	}

	AuditEventEntity(
			Instant occurredAt,
			Long actorStaffId,
			AuditAction action,
			String subjectType,
			String subjectId,
			String correlationId) {
		this.occurredAt = occurredAt;
		this.actorStaffId = actorStaffId;
		this.action = action;
		this.subjectType = subjectType;
		this.subjectId = subjectId;
		this.correlationId = correlationId;
	}
}
