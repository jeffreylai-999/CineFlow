package com.cineflow.identity;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface RefreshTokenFamilyRepository extends JpaRepository<RefreshTokenFamilyEntity, Long> {

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update RefreshTokenFamilyEntity f set f.revokedAt = :now where f.staff.id = :staffId and f.revokedAt is null")
	int revokeAllForStaff(@Param("staffId") Long staffId, @Param("now") Instant now);
}
