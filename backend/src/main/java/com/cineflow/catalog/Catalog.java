package com.cineflow.catalog;

import java.util.List;

/**
 * Catalog module interface: available Movies for public browsing.
 */
public interface Catalog {

	List<MovieResponse> listAvailableMovies();
}
