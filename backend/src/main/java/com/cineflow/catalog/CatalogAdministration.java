package com.cineflow.catalog;

import java.util.List;

public interface CatalogAdministration {

	List<MovieSearchHit> search(String query);

	MovieAdminResponse importMovie(long actorStaffId, String externalId, Integer runtimeMinutes, String ageRating);

	MovieAdminResponse refresh(long actorStaffId, long movieId);

	MovieAdminResponse updateSchedulingFields(long movieId, int runtimeMinutes, String ageRating);

	List<MovieAdminResponse> listMovies();
}
