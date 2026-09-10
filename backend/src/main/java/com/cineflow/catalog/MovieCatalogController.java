package com.cineflow.catalog;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = "/api/movies", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Catalog")
public class MovieCatalogController {

	private final Catalog catalog;

	MovieCatalogController(Catalog catalog) {
		this.catalog = catalog;
	}

	@GetMapping
	@Operation(summary = "List available Movies")
	public List<MovieResponse> listMovies() {
		return catalog.listAvailableMovies();
	}
}
