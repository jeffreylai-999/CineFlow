package com.cineflow.identity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_token_families", schema = "cineflow")
class RefreshTokenFamilyEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "staff_account_id", nullable = false)
	private StaffAccountEntity staff;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected RefreshTokenFamilyEntity() {
	}

	RefreshTokenFamilyEntity(StaffAccountEntity staff, Instant createdAt) {
		this.staff = staff;
		this.createdAt = createdAt;
	}

	Long getId() {
		return id;
	}

	StaffAccountEntity getStaff() {
		return staff;
	}

	Instant getRevokedAt() {
		return revokedAt;
	}

	boolean isRevoked() {
		return revokedAt != null;
	}

	void revoke(Instant revokedAt) {
		this.revokedAt = revokedAt;
	}
}
