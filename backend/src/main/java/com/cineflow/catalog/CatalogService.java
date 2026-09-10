package com.cineflow.catalog;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CatalogService implements Catalog {

	private final MovieRepository movieRepository;

	CatalogService(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<MovieResponse> listAvailableMovies() {
		return movieRepository.findByArchivedAtIsNullOrderByTitleAsc().stream()
			.map(MovieEntity::toResponse)
			.toList();
	}
}
