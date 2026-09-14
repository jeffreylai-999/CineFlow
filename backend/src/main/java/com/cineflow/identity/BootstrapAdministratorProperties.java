package com.cineflow.identity;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cineflow.bootstrap.administrator")
public record BootstrapAdministratorProperties(String username, String password) {
}
