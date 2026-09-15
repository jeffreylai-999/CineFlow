package com.cineflow.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "catalog_settings", schema = "cineflow")
class CatalogSettingsEntity {

	@Id
	private Integer id;

	@Column(name = "active_provider", nullable = false)
	private String activeProvider;

	protected CatalogSettingsEntity() {
	}

	Integer getId() {
		return id;
	}

	String getActiveProvider() {
		return activeProvider;
	}

	void setActiveProvider(String activeProvider) {
		this.activeProvider = activeProvider;
	}
}
