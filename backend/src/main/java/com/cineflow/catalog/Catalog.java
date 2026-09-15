package com.cineflow.catalog;

import java.util.List;
import java.util.Optional;

/**
 * Catalog module interface: available Movies for public browsing.
 */
public interface Catalog {

	List<MovieResponse> listAvailableMovies();

	Optional<MovieForSchedule> findMovieForSchedule(long movieId);
}
