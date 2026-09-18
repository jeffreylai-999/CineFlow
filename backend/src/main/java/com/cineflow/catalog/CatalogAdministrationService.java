package com.cineflow.catalog;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.cineflow.audit.Audit;
import com.cineflow.audit.AuditAction;

@Service
class CatalogAdministrationService implements CatalogAdministration {

	private static final String PROVIDER_IDENTITY_CONSTRAINT = "movies_provider_external_unique";

	private final MovieMetadataProviders movieMetadataProviders;
	private final MovieRepository movieRepository;
	private final Audit audit;
	private final Clock clock;
	private final TransactionTemplate transactions;

	CatalogAdministrationService(
			MovieMetadataProviders movieMetadataProviders,
			MovieRepository movieRepository,
			Audit audit,
			Clock clock,
			PlatformTransactionManager transactionManager) {
		this.movieMetadataProviders = movieMetadataProviders;
		this.movieRepository = movieRepository;
		this.audit = audit;
		this.clock = clock;
		this.transactions = new TransactionTemplate(transactionManager);
	}

	@Override
	public List<MovieSearchHit> search(String query) {
		if (query == null || query.isBlank()) {
			throw CatalogException.invalidRequest();
		}
		return movieMetadataProviders.active().search(query);
	}

	@Override
	public MovieAdminResponse importMovie(
			long actorStaffId,
			String providerId,
			String externalId,
			Integer runtimeMinutes,
			String ageRating) {
		MovieMetadataProvider provider = movieMetadataProviders.requireActive(providerId);
		if (alreadyImported(provider.providerId(), externalId)) {
			throw CatalogException.duplicateImport();
		}
		MovieProviderRecord record = provider.fetch(externalId);
		return Objects.requireNonNull(transactions.execute(
				status -> persistImportedMovie(actorStaffId, providerId, record, runtimeMinutes, ageRating)));
	}

	@Override
	public MovieAdminResponse refresh(long actorStaffId, long movieId) {
		MovieEntity movie = movieRepository.findById(movieId).orElseThrow(CatalogException::movieNotFound);
		MovieProviderRecord record = movieMetadataProviders.source(movie.getSourceProvider()).fetch(movie.getExternalId());
		return Objects.requireNonNull(transactions.execute(status -> applyRefresh(actorStaffId, movieId, record)));
	}

	@Override
	@Transactional
	public MovieAdminResponse archive(long actorStaffId, long movieId) {
		MovieEntity movie = movieRepository.findById(movieId).orElseThrow(CatalogException::movieNotFound);
		if (movie.getArchivedAt() == null) {
			movie.archive(clock.instant());
			audit.record(actorStaffId, AuditAction.MOVIE_ARCHIVED, "movie", Long.toString(movie.getId()));
		}
		return movie.toAdminResponse(clock.instant());
	}

	@Override
	@Transactional
	public MovieAdminResponse updateSchedulingFields(long movieId, int runtimeMinutes, String ageRating) {
		if (runtimeMinutes <= 0 || ageRating == null || ageRating.isBlank()) {
			throw CatalogException.schedulingFieldsRequired();
		}
		MovieEntity movie = movieRepository.findById(movieId).orElseThrow(CatalogException::movieNotFound);
		movie.updateScheduling(runtimeMinutes, ageRating);
		try {
			movieRepository.flush();
		}
		catch (DataIntegrityViolationException exception) {
			if (isShowtimeOccupancyConflict(exception)) {
				throw CatalogException.showtimeOverlap();
			}
			throw exception;
		}
		return movie.toAdminResponse(clock.instant());
	}

	@Override
	@Transactional(readOnly = true)
	public List<MovieAdminResponse> listMovies() {
		Instant now = clock.instant();
		return movieRepository.findAllByOrderByTitleAsc().stream()
				.map(movie -> movie.toAdminResponse(now))
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public MovieProviderSettingsResponse providers() {
		return movieMetadataProviders.current();
	}

	@Override
	public MovieProviderSettingsResponse selectProvider(long actorStaffId, String providerId) {
		return movieMetadataProviders.select(actorStaffId, providerId);
	}

	private MovieAdminResponse persistImportedMovie(
			long actorStaffId,
			String requestedProviderId,
			MovieProviderRecord record,
			Integer runtimeMinutes,
			String ageRating) {
		movieMetadataProviders.requireStillActive(requestedProviderId);
		if (alreadyImported(record.providerId(), record.externalId())) {
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
			if (isProviderIdentityConflict(exception)) {
				throw CatalogException.duplicateImport();
			}
			throw CatalogException.saveFailed();
		}
		audit.record(actorStaffId, AuditAction.MOVIE_IMPORTED, "movie", Long.toString(movie.getId()));
		return movie.toAdminResponse(clock.instant());
	}

	private MovieAdminResponse applyRefresh(long actorStaffId, long movieId, MovieProviderRecord record) {
		MovieEntity movie = movieRepository.findById(movieId).orElseThrow(CatalogException::movieNotFound);
		movie.refreshDescriptive(
				record.title(),
				record.synopsis() == null ? "" : record.synopsis(),
				record.genre(),
				record.posterUrl(),
				clock.instant());
		audit.record(actorStaffId, AuditAction.MOVIE_REFRESHED, "movie", Long.toString(movie.getId()));
		return movie.toAdminResponse(clock.instant());
	}

	private boolean alreadyImported(String providerId, String externalId) {
		if (providerId == null || providerId.isBlank() || externalId == null || externalId.isBlank()) {
			return false;
		}
		return movieRepository.findBySourceProviderAndExternalId(providerId, externalId).isPresent();
	}

	static boolean isShowtimeOccupancyConflict(DataIntegrityViolationException exception) {
		Throwable current = exception;
		while (current != null) {
			String message = current.getMessage();
			if (message != null && message.contains("showtimes_hall_occupancy_excl")) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}

	static boolean isProviderIdentityConflict(DataIntegrityViolationException exception) {
		Throwable current = exception;
		while (current != null) {
			String message = current.getMessage();
			if (message != null && message.contains(PROVIDER_IDENTITY_CONSTRAINT)) {
				return true;
			}
			current = current.getCause();
		}
		return false;
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
