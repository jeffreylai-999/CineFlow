package com.cineflow.catalog;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({ TmdbProperties.class, CatalogProperties.class })
class CatalogConfiguration {

	@Bean
	RestClient tmdbRestClient(TmdbProperties properties) {
		String baseUrl = properties.baseUrl() == null || properties.baseUrl().isBlank()
				? "https://api.themoviedb.org/3"
				: properties.baseUrl();
		return RestClient.builder().baseUrl(baseUrl).build();
	}
}
