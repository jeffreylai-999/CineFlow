package com.cineflow.catalog;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({ TmdbProperties.class, OmdbProperties.class, CatalogProperties.class })
class CatalogConfiguration {

	@Bean
	RestClient tmdbRestClient(TmdbProperties properties) {
		return restClient(
				properties.baseUrl(),
				"https://api.themoviedb.org/3",
				properties.connectTimeout(),
				properties.readTimeout());
	}

	@Bean
	RestClient omdbRestClient(OmdbProperties properties) {
		return restClient(
				properties.baseUrl(),
				"https://www.omdbapi.com",
				properties.connectTimeout(),
				properties.readTimeout());
	}

	private static RestClient restClient(String baseUrl, String fallback, Duration connectTimeout,
			Duration readTimeout) {
		String resolved = baseUrl == null || baseUrl.isBlank() ? fallback : baseUrl;
		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(readTimeout);
		return RestClient.builder().baseUrl(resolved).requestFactory(requestFactory).build();
	}
}
