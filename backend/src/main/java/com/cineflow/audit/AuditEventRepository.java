package com.cineflow.audit;

import org.springframework.data.jpa.repository.JpaRepository;

interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {
}
