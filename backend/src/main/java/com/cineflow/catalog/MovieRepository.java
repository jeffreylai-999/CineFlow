package com.cineflow.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

interface MovieRepository extends JpaRepository<MovieEntity, Long> {

	List<MovieEntity> findAllByOrderByTitleAsc();

	Optional<MovieEntity> findBySourceProviderAndExternalId(String sourceProvider, String externalId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select m from MovieEntity m where m.id = :id")
	Optional<MovieEntity> findByIdForUpdate(@Param("id") Long id);
}
