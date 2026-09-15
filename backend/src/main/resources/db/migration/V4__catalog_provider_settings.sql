-- Singleton Administrator selection for the active Movie metadata provider.
CREATE TABLE cineflow.catalog_settings (
    id              INTEGER PRIMARY KEY,
    active_provider TEXT    NOT NULL,
    CONSTRAINT catalog_settings_singleton CHECK (id = 1),
    CONSTRAINT catalog_settings_provider_check CHECK (active_provider IN ('tmdb', 'omdb'))
);

INSERT INTO cineflow.catalog_settings (id, active_provider)
VALUES (1, 'tmdb');
