package com.cineflow.catalog;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

interface CatalogSettingsRepository extends JpaRepository<CatalogSettingsEntity, Integer> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from CatalogSettingsEntity s where s.id = :id")
	Optional<CatalogSettingsEntity> findByIdForUpdate(@Param("id") Integer id);
}
