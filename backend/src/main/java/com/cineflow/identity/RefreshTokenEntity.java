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
@Table(name = "refresh_tokens", schema = "cineflow")
class RefreshTokenEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "family_id", nullable = false)
	private RefreshTokenFamilyEntity family;

	@Column(name = "token_hash", nullable = false, unique = true)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "replaced_by_id")
	private RefreshTokenEntity replacedBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected RefreshTokenEntity() {
	}

	RefreshTokenEntity(
			RefreshTokenFamilyEntity family,
			String tokenHash,
			Instant expiresAt,
			Instant createdAt) {
		this.family = family;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
		this.createdAt = createdAt;
	}

	Long getId() {
		return id;
	}

	RefreshTokenFamilyEntity getFamily() {
		return family;
	}

	Instant getExpiresAt() {
		return expiresAt;
	}

	boolean isConsumed() {
		return revokedAt != null || replacedBy != null;
	}

	void replaceWith(RefreshTokenEntity replacement, Instant revokedAt) {
		this.replacedBy = replacement;
		this.revokedAt = revokedAt;
	}

	void revoke(Instant revokedAt) {
		this.revokedAt = revokedAt;
	}
}
