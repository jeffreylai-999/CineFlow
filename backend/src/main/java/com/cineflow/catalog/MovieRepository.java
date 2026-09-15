package com.cineflow.catalog;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface MovieRepository extends JpaRepository<MovieEntity, Long> {

	List<MovieEntity> findByArchivedAtIsNullOrderByTitleAsc();

	List<MovieEntity> findAllByOrderByTitleAsc();

	Optional<MovieEntity> findBySourceProviderAndExternalId(String sourceProvider, String externalId);
}
