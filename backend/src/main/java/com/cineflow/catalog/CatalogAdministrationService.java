package com.cineflow.catalog;

import java.time.Clock;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineflow.audit.Audit;
import com.cineflow.audit.AuditAction;

@Service
class CatalogAdministrationService implements CatalogAdministration {

	private final MovieMetadataProvider movieMetadataProvider;
	private final MovieRepository movieRepository;
	private final Audit audit;
	private final Clock clock;

	CatalogAdministrationService(
			MovieMetadataProvider movieMetadataProvider,
			MovieRepository movieRepository,
			Audit audit,
			Clock clock) {
		this.movieMetadataProvider = movieMetadataProvider;
		this.movieRepository = movieRepository;
		this.audit = audit;
		this.clock = clock;
	}

	@Override
	public List<MovieSearchHit> search(String query) {
		if (query == null || query.isBlank()) {
			throw CatalogException.invalidRequest();
		}
		return movieMetadataProvider.search(query);
	}

	@Override
	@Transactional
	public MovieAdminResponse importMovie(long actorStaffId, String externalId, Integer runtimeMinutes, String ageRating) {
		MovieProviderRecord record = movieMetadataProvider.fetch(externalId);
		if (movieRepository.findBySourceProviderAndExternalId(record.providerId(), record.externalId()).isPresent()) {
			throw CatalogException.duplicateImport();
		}
		int runtime = firstPositive(runtimeMinutes, record.runtimeMinutes());
		String rating = firstText(ageRating, record.ageRating());
		if (runtime <= 0 || rating == null || rating.isBlank()) {
			throw CatalogException.schedulingFieldsRequired();
		}
		MovieEntity movie = MovieEntity.imported(
				record.title(),
				record.synopsis() == null ? "" : record.synopsis(),
				record.genre(),
				runtime,
				rating,
				record.posterUrl(),
				record.providerId(),
				record.externalId(),
				clock.instant());
		try {
			movieRepository.saveAndFlush(movie);
		}
		catch (DataIntegrityViolationException exception) {
			throw CatalogException.duplicateImport();
		}
		audit.record(actorStaffId, AuditAction.MOVIE_IMPORTED, "movie", Long.toString(movie.getId()));
		return movie.toAdminResponse();
	}

	@Override
	@Transactional
	public MovieAdminResponse refresh(long actorStaffId, long movieId) {
		MovieEntity movie = movieRepository.findById(movieId).orElseThrow(CatalogException::movieNotFound);
		if (!movieMetadataProvider.providerId().equals(movie.getSourceProvider())) {
			throw CatalogException.providerUnavailable();
		}
		MovieProviderRecord record = movieMetadataProvider.fetch(movie.getExternalId());
		movie.refreshDescriptive(
				record.title(),
				record.synopsis() == null ? "" : record.synopsis(),
				record.genre(),
				record.posterUrl(),
				clock.instant());
		audit.record(actorStaffId, AuditAction.MOVIE_REFRESHED, "movie", Long.toString(movie.getId()));
		return movie.toAdminResponse();
	}

	@Override
	@Transactional
	public MovieAdminResponse updateSchedulingFields(long movieId, int runtimeMinutes, String ageRating) {
		if (runtimeMinutes <= 0 || ageRating == null || ageRating.isBlank()) {
			throw CatalogException.schedulingFieldsRequired();
		}
		MovieEntity movie = movieRepository.findById(movieId).orElseThrow(CatalogException::movieNotFound);
		movie.updateScheduling(runtimeMinutes, ageRating);
		return movie.toAdminResponse();
	}

	@Override
	@Transactional(readOnly = true)
	public List<MovieAdminResponse> listMovies() {
		return movieRepository.findAllByOrderByTitleAsc().stream().map(MovieEntity::toAdminResponse).toList();
	}

	private static int firstPositive(Integer preferred, Integer fallback) {
		if (preferred != null && preferred > 0) {
			return preferred;
		}
		if (fallback != null && fallback > 0) {
			return fallback;
		}
		return 0;
	}

	private static String firstText(String preferred, String fallback) {
		if (preferred != null && !preferred.isBlank()) {
			return preferred;
		}
		return fallback;
	}
}
