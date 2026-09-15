package com.cineflow.catalog;

import java.util.List;

public record MovieProviderSettingsResponse(String activeProviderId, List<MovieProviderOption> providers) {
}
