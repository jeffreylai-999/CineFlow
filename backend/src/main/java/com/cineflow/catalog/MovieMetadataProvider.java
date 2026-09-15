package com.cineflow.catalog;

import java.util.List;

public interface MovieMetadataProvider {

	String providerId();

	List<MovieSearchHit> search(String query);

	MovieProviderRecord fetch(String externalId);
}
