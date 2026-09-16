package com.cineflow.catalog;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/admin/movie-providers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Catalog administration")
public class MovieProviderController {

	private final CatalogAdministration catalogAdministration;

	MovieProviderController(CatalogAdministration catalogAdministration) {
		this.catalogAdministration = catalogAdministration;
	}

	@GetMapping
	@Operation(
			summary = "List Movie metadata providers",
			description = "Returns configured providers plus the stored active provider when that provider has no credentials, so radios stay honest. Search stays catalog.provider_not_configured until an Administrator selects a configured provider. CineFlow does not fail over or auto-select another provider.")
	public MovieProviderSettingsResponse list() {
		return catalogAdministration.providers();
	}

	@PutMapping("/active")
	@Operation(summary = "Select the active Movie metadata provider")
	public MovieProviderSettingsResponse select(
			@Valid @RequestBody SelectMovieProviderRequest request,
			Authentication authentication) {
		Jwt jwt = (Jwt) authentication.getPrincipal();
		return catalogAdministration.selectProvider(Long.parseLong(jwt.getSubject()), request.providerId());
	}
}
