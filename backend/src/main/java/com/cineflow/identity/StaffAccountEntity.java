package com.cineflow.identity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "staff_accounts", schema = "cineflow")
class StaffAccountEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StaffRole role;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected StaffAccountEntity() {
	}

	StaffAccountEntity(String username, String passwordHash, StaffRole role, Instant createdAt) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.role = role;
		this.active = true;
		this.createdAt = createdAt;
	}

	Long getId() {
		return id;
	}

	String getUsername() {
		return username;
	}

	String getPasswordHash() {
		return passwordHash;
	}

	void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	StaffRole getRole() {
		return role;
	}

	boolean isActive() {
		return active;
	}

	void deactivate() {
		this.active = false;
	}

	StaffProfile toProfile() {
		return new StaffProfile(id, username, role);
	}

	StaffAccountSummary toSummary() {
		return new StaffAccountSummary(id, username, role, active);
	}
}
