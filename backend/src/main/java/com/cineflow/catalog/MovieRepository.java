package com.cineflow.catalog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface MovieRepository extends JpaRepository<MovieEntity, Long> {

	List<MovieEntity> findByArchivedAtIsNullOrderByTitleAsc();
}
