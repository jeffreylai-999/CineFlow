package com.cineflow.audit;

import java.time.Clock;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cineflow.platform.CorrelationIdFilter;

@Service
class AuditService implements Audit {

	private final AuditEventRepository auditEventRepository;
	private final Clock clock;

	AuditService(AuditEventRepository auditEventRepository, Clock clock) {
		this.auditEventRepository = auditEventRepository;
		this.clock = clock;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void record(Long actorStaffId, AuditAction action, String subjectType, String subjectId) {
		auditEventRepository.save(new AuditEventEntity(
				clock.instant(),
				actorStaffId,
				action,
				subjectType,
				subjectId,
				MDC.get(CorrelationIdFilter.MDC_KEY)));
	}
}
