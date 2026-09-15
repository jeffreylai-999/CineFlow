package com.cineflow.catalog;

import java.net.http.HttpClient;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({ TmdbProperties.class, CatalogProperties.class })
class CatalogConfiguration {

	@Bean
	RestClient tmdbRestClient(TmdbProperties properties) {
		String baseUrl = properties.baseUrl() == null || properties.baseUrl().isBlank()
				? "https://api.themoviedb.org/3"
				: properties.baseUrl();
		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.connectTimeout()).build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(properties.readTimeout());
		return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
	}
}
