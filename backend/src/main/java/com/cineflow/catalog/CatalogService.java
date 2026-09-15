package com.cineflow.catalog;

import java.util.List;
import java.util.Optional;

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

	@Override
	@Transactional(readOnly = true)
	public Optional<MovieForSchedule> findMovieForSchedule(long movieId) {
		return movieRepository.findById(movieId).map(movie -> new MovieForSchedule(
				movie.getId(),
				movie.getTitle(),
				movie.getRuntimeMinutes(),
				movie.getArchivedAt() != null));
	}
}
