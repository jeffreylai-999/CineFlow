package com.cineflow.catalog;

import java.util.Optional;

/**
 * Catalog module interface: Movies for scheduling.
 */
public interface Catalog {

	Optional<MovieForSchedule> findMovieForSchedule(long movieId);
}
