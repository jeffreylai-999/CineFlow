package com.cineflow.catalog;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/admin/movies", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Catalog administration")
public class MovieAdministrationController {

	private final CatalogAdministration catalogAdministration;
	private final CatalogSearchRateLimiter searchRateLimiter;

	MovieAdministrationController(
			CatalogAdministration catalogAdministration,
			CatalogSearchRateLimiter searchRateLimiter) {
		this.catalogAdministration = catalogAdministration;
		this.searchRateLimiter = searchRateLimiter;
	}

	@GetMapping
	@Operation(summary = "List managed Movies")
	public List<MovieAdminResponse> listMovies() {
		return catalogAdministration.listMovies();
	}

	@GetMapping("/search")
	@Operation(summary = "Search the active Movie metadata provider")
	public List<MovieSearchHit> search(@RequestParam String query, Authentication authentication) {
		searchRateLimiter.check("staff:" + staffId(authentication));
		return catalogAdministration.search(query);
	}

	@PostMapping("/import")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Import a Movie from the active provider")
	public MovieAdminResponse importMovie(@Valid @RequestBody ImportMovieRequest request, Authentication authentication) {
		return catalogAdministration.importMovie(
				staffId(authentication),
				request.providerId(),
				request.externalId(),
				request.runtimeMinutes(),
				request.ageRating());
	}

	@PostMapping("/{movieId}/refresh")
	@Operation(summary = "Refresh descriptive metadata from the Movie source provider")
	public MovieAdminResponse refresh(@PathVariable long movieId, Authentication authentication) {
		return catalogAdministration.refresh(staffId(authentication), movieId);
	}

	@PostMapping("/{movieId}/archive")
	@Operation(summary = "Archive a Movie so it leaves new scheduling and catalog choices")
	public MovieAdminResponse archive(@PathVariable long movieId, Authentication authentication) {
		return catalogAdministration.archive(staffId(authentication), movieId);
	}

	@PatchMapping("/{movieId}")
	@Operation(summary = "Update locally controlled runtime and age rating")
	public MovieAdminResponse updateSchedulingFields(
			@PathVariable long movieId,
			@Valid @RequestBody UpdateMovieRequest request) {
		return catalogAdministration.updateSchedulingFields(movieId, request.runtimeMinutes(), request.ageRating());
	}

	private static long staffId(Authentication authentication) {
		Jwt jwt = (Jwt) authentication.getPrincipal();
		return Long.parseLong(jwt.getSubject());
	}
}
