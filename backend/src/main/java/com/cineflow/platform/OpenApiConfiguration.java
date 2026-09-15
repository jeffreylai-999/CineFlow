package com.cineflow.platform;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

	@Bean
	OpenAPI cineFlowOpenApi() {
		return new OpenAPI()
			.info(new Info()
				.title("CineFlow API")
				.version("0.1.0")
				.description("Movie catalog, Staff authentication, and future cinema booking interfaces."));
	}
}
