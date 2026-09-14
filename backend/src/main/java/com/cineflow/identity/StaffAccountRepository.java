package com.cineflow.identity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface StaffAccountRepository extends JpaRepository<StaffAccountEntity, Long> {

	Optional<StaffAccountEntity> findByUsername(String username);

	long countByRole(StaffRole role);
}
