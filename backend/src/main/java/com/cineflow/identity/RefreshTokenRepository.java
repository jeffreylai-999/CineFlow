package com.cineflow.identity;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

	@EntityGraph(attributePaths = { "family", "family.staff" })
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select t from RefreshTokenEntity t where t.tokenHash = :tokenHash")
	Optional<RefreshTokenEntity> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update RefreshTokenEntity t set t.revokedAt = :now where t.family.id = :familyId and t.revokedAt is null")
	int revokeAllInFamily(@Param("familyId") Long familyId, @Param("now") Instant now);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			update RefreshTokenEntity t set t.revokedAt = :now
			where t.family.staff.id = :staffId and t.revokedAt is null
			""")
	int revokeAllForStaff(@Param("staffId") Long staffId, @Param("now") Instant now);
}
