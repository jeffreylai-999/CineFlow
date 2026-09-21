package com.cineflow.platform;

import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HttpProperties.class)
class ClockConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
