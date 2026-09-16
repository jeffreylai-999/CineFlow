package com.cineflow.catalog;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineflow.audit.Audit;
import com.cineflow.audit.AuditAction;

@Service
class MovieMetadataProviders {

	static final String TMDB = "tmdb";
	static final String OMDB = "omdb";

	private final MovieMetadataProvider tmdb;
	private final MovieMetadataProvider omdb;
	private final TmdbProperties tmdbProperties;
	private final OmdbProperties omdbProperties;
	private final CatalogSettingsRepository settings;
	private final Audit audit;

	MovieMetadataProviders(
			@Qualifier("tmdbMovieMetadataProvider") MovieMetadataProvider tmdb,
			@Qualifier("omdbMovieMetadataProvider") MovieMetadataProvider omdb,
			TmdbProperties tmdbProperties,
			OmdbProperties omdbProperties,
			CatalogSettingsRepository settings,
			Audit audit) {
		this.tmdb = tmdb;
		this.omdb = omdb;
		this.tmdbProperties = tmdbProperties;
		this.omdbProperties = omdbProperties;
		this.settings = settings;
		this.audit = audit;
	}

	MovieMetadataProvider active() {
		return requireConfigured(activeProviderId());
	}

	MovieMetadataProvider requireActive(String requestedProviderId) {
		if (requestedProviderId == null || requestedProviderId.isBlank()) {
			throw CatalogException.invalidRequest();
		}
		String active = activeProviderId();
		if (!requestedProviderId.equals(active)) {
			throw CatalogException.providerMismatch();
		}
		return requireConfigured(active);
	}

	void requireStillActive(String requestedProviderId) {
		CatalogSettingsEntity row = settings.findByIdForUpdate(1).orElseThrow(CatalogException::saveFailed);
		if (requestedProviderId == null
				|| requestedProviderId.isBlank()
				|| !requestedProviderId.equals(row.getActiveProvider())) {
			throw CatalogException.providerMismatch();
		}
	}

	MovieMetadataProvider source(String providerId) {
		if (TMDB.equals(providerId)) {
			return tmdb;
		}
		if (OMDB.equals(providerId)) {
			return omdb;
		}
		throw CatalogException.invalidRequest();
	}

	String activeProviderId() {
		return settings.findById(1).map(CatalogSettingsEntity::getActiveProvider).orElse(TMDB);
	}

	List<MovieProviderOption> configured() {
		List<MovieProviderOption> options = new ArrayList<>();
		if (tmdbProperties.configured()) {
			options.add(new MovieProviderOption(TMDB, "TMDB"));
		}
		if (omdbProperties.configured()) {
			options.add(new MovieProviderOption(OMDB, "OMDb"));
		}
		return List.copyOf(options);
	}

	private List<MovieProviderOption> listed(String active) {
		List<MovieProviderOption> options = new ArrayList<>(configured());
		if (options.stream().noneMatch(option -> option.id().equals(active))) {
			if (TMDB.equals(active)) {
				options.addFirst(new MovieProviderOption(TMDB, "TMDB"));
			}
			else if (OMDB.equals(active)) {
				options.add(new MovieProviderOption(OMDB, "OMDb"));
			}
		}
		return List.copyOf(options);
	}

	MovieProviderSettingsResponse current() {
		String active = activeProviderId();
		return new MovieProviderSettingsResponse(active, listed(active));
	}

	@Transactional
	MovieProviderSettingsResponse select(long actorStaffId, String providerId) {
		if (providerId == null || providerId.isBlank()) {
			throw CatalogException.invalidRequest();
		}
		if (!TMDB.equals(providerId) && !OMDB.equals(providerId)) {
			throw CatalogException.invalidRequest();
		}
		if (configured().stream().noneMatch(option -> option.id().equals(providerId))) {
			throw CatalogException.providerNotConfigured();
		}
		CatalogSettingsEntity row = settings.findByIdForUpdate(1).orElseThrow(CatalogException::saveFailed);
		row.setActiveProvider(providerId);
		settings.save(row);
		audit.record(actorStaffId, AuditAction.MOVIE_PROVIDER_SELECTED, "movie_provider", providerId);
		return current();
	}

	private MovieMetadataProvider requireConfigured(String providerId) {
		if (configured().stream().noneMatch(option -> option.id().equals(providerId))) {
			throw CatalogException.providerNotConfigured();
		}
		return source(providerId);
	}
}
