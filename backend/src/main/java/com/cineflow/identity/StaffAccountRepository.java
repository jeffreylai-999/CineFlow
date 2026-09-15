package com.cineflow.identity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

interface StaffAccountRepository extends JpaRepository<StaffAccountEntity, Long> {

	Optional<StaffAccountEntity> findByUsername(String username);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from StaffAccountEntity s where s.username = :username")
	Optional<StaffAccountEntity> findByUsernameForUpdate(@Param("username") String username);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from StaffAccountEntity s where s.id = :id")
	Optional<StaffAccountEntity> findByIdForUpdate(@Param("id") Long id);

	long countByRole(StaffRole role);

	List<StaffAccountEntity> findAllByOrderByUsernameAsc();
}
