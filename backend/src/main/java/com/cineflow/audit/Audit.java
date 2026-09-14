package com.cineflow.audit;

public interface Audit {

	void record(Long actorStaffId, AuditAction action, String subjectType, String subjectId);
}
