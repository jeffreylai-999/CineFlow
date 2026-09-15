package com.cineflow.identity;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineflow.audit.Audit;
import com.cineflow.audit.AuditAction;

@Service
class IdentityService implements Identity {

	private final StaffAccountRepository staffAccounts;
	private final RefreshTokenFamilyRepository families;
	private final RefreshTokenRepository refreshTokens;
	private final PasswordEncoder passwordEncoder;
	private final AccessTokens accessTokens;
	private final AuthProperties authProperties;
	private final Audit audit;
	private final Clock clock;
	private final ApplicationEventPublisher events;
	private final String dummyPasswordHash;

	IdentityService(
			StaffAccountRepository staffAccounts,
			RefreshTokenFamilyRepository families,
			RefreshTokenRepository refreshTokens,
			PasswordEncoder passwordEncoder,
			AccessTokens accessTokens,
			AuthProperties authProperties,
			Audit audit,
			Clock clock,
			ApplicationEventPublisher events) {
		this.staffAccounts = staffAccounts;
		this.families = families;
		this.refreshTokens = refreshTokens;
		this.passwordEncoder = passwordEncoder;
		this.accessTokens = accessTokens;
		this.authProperties = authProperties;
		this.audit = audit;
		this.clock = clock;
		this.events = events;
		this.dummyPasswordHash = passwordEncoder.encode("cineflow-unused-dummy-password");
	}

	@Override
	@Transactional(noRollbackFor = IdentityException.class)
	public StaffSession login(String username, String password) {
		StaffAccountEntity staff = staffAccounts.findByUsernameForUpdate(username).orElse(null);
		String hash = staff != null && staff.isActive() ? staff.getPasswordHash() : dummyPasswordHash;
		boolean credentialsMatch = passwordEncoder.matches(password, hash);
		if (staff == null || !staff.isActive() || !credentialsMatch) {
			audit.record(staff == null ? null : staff.getId(), AuditAction.STAFF_LOGIN_FAILURE, "staff", username);
			throw IdentityException.invalidCredentials();
		}
		StaffSession session = issueNewFamily(staff);
		audit.record(staff.getId(), AuditAction.STAFF_LOGIN, "staff", Long.toString(staff.getId()));
		return session;
	}

	@Override
	@Transactional(noRollbackFor = IdentityException.class)
	public StaffSession refresh(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw IdentityException.unauthorized();
		}
		Instant now = clock.instant();
		LockedRefresh locked = lockRefresh(refreshToken);
		RefreshTokenEntity current = locked.token();
		RefreshTokenFamilyEntity family = current.getFamily();
		StaffAccountEntity staff = locked.staff();
		if (current.isConsumed() || family.isRevoked()) {
			revokeFamily(family, now);
			audit.record(staff.getId(), AuditAction.TOKEN_REUSE, "refresh_token_family", Long.toString(family.getId()));
			throw IdentityException.tokenReused();
		}
		if (!current.getExpiresAt().isAfter(now) || !staff.isActive()) {
			revokeFamily(family, now);
			throw IdentityException.unauthorized();
		}
		IssuedRefreshToken rotated = rotate(current, now);
		audit.record(staff.getId(), AuditAction.TOKEN_REFRESH, "refresh_token_family", Long.toString(family.getId()));
		return new StaffSession(
				accessTokens.issue(staff),
				authProperties.accessTokenTtl(),
				rotated.plaintext(),
				authProperties.refreshTokenTtl(),
				staff.toProfile());
	}

	@Override
	@Transactional
	public void logout(String refreshToken) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw IdentityException.unauthorized();
		}
		Instant now = clock.instant();
		LockedRefresh locked = lockRefresh(refreshToken);
		RefreshTokenFamilyEntity family = locked.token().getFamily();
		revokeFamily(family, now);
		audit.record(locked.staff().getId(), AuditAction.STAFF_LOGOUT, "refresh_token_family", Long.toString(family.getId()));
	}

	@Override
	@Transactional
	public void deactivate(long actorStaffId, long targetStaffId) {
		StaffAccountEntity actor = requireAdministrator(actorStaffId);
		StaffAccountEntity target = staffAccounts.findByIdForUpdate(targetStaffId).orElseThrow(IdentityException::unauthorized);
		if (target.getRole() != StaffRole.BOOKING_STAFF) {
			throw IdentityException.forbidden();
		}
		target.deactivate();
		revokeAllForStaff(target.getId());
		audit.record(actor.getId(), AuditAction.STAFF_DEACTIVATED, "staff", Long.toString(target.getId()));
		events.publishEvent(new StaffDeactivated(target.getId()));
	}

	@Override
	@Transactional
	public void resetPassword(long actorStaffId, long targetStaffId, String newPassword) {
		StaffAccountEntity actor = requireAdministrator(actorStaffId);
		StaffAccountEntity target = staffAccounts.findByIdForUpdate(targetStaffId).orElseThrow(IdentityException::unauthorized);
		if (target.getRole() != StaffRole.BOOKING_STAFF) {
			throw IdentityException.forbidden();
		}
		requireStrongPassword(newPassword);
		target.setPasswordHash(passwordEncoder.encode(newPassword));
		revokeAllForStaff(target.getId());
		audit.record(actor.getId(), AuditAction.STAFF_PASSWORD_RESET, "staff", Long.toString(target.getId()));
	}

	@Override
	@Transactional(readOnly = true)
	public List<StaffAccountSummary> listStaffAccounts(long actorStaffId) {
		requireAdministrator(actorStaffId);
		return staffAccounts.findAllByOrderByUsernameAsc().stream()
			.map(StaffAccountEntity::toSummary)
			.toList();
	}

	@Override
	@Transactional
	public StaffAccountSummary createBookingStaff(long actorStaffId, String username, String password) {
		StaffAccountEntity actor = requireAdministrator(actorStaffId);
		String normalized = username.trim();
		requireStrongPassword(password);
		if (staffAccounts.findByUsername(normalized).isPresent()) {
			throw IdentityException.usernameConflict();
		}
		StaffAccountEntity created;
		try {
			created = staffAccounts.saveAndFlush(new StaffAccountEntity(
					normalized,
					passwordEncoder.encode(password),
					StaffRole.BOOKING_STAFF,
					clock.instant()));
		}
		catch (DataIntegrityViolationException ex) {
			throw IdentityException.usernameConflict();
		}
		audit.record(actor.getId(), AuditAction.STAFF_CREATED, "staff", Long.toString(created.getId()));
		return created.toSummary();
	}

	@Override
	@Transactional(readOnly = true)
	public StaffProfile me(long staffId) {
		StaffAccountEntity staff = staffAccounts.findById(staffId).orElseThrow(IdentityException::unauthorized);
		if (!staff.isActive()) {
			throw IdentityException.unauthorized();
		}
		return staff.toProfile();
	}

	private StaffAccountEntity requireAdministrator(long actorStaffId) {
		StaffAccountEntity actor = staffAccounts.findById(actorStaffId).orElseThrow(IdentityException::unauthorized);
		if (!actor.isActive() || actor.getRole() != StaffRole.ADMINISTRATOR) {
			throw IdentityException.forbidden();
		}
		return actor;
	}

	private static void requireStrongPassword(String password) {
		if (password == null
				|| password.length() < PasswordResetRequest.MIN_LENGTH
				|| password.getBytes(StandardCharsets.UTF_8).length > PasswordResetRequest.MAX_BCRYPT_BYTES) {
			throw IdentityException.invalidRequest();
		}
	}

	private StaffSession issueNewFamily(StaffAccountEntity staff) {
		Instant now = clock.instant();
		RefreshTokenFamilyEntity family = families.save(new RefreshTokenFamilyEntity(staff, now));
		IssuedRefreshToken issued = persistToken(family, now);
		return new StaffSession(
				accessTokens.issue(staff),
				authProperties.accessTokenTtl(),
				issued.plaintext(),
				authProperties.refreshTokenTtl(),
				staff.toProfile());
	}

	private IssuedRefreshToken rotate(RefreshTokenEntity current, Instant now) {
		IssuedRefreshToken issued = persistToken(current.getFamily(), now);
		current.replaceWith(issued.entity(), now);
		return issued;
	}

	private IssuedRefreshToken persistToken(RefreshTokenFamilyEntity family, Instant now) {
		String plaintext = RefreshTokenCodec.generate();
		RefreshTokenEntity entity = refreshTokens.save(new RefreshTokenEntity(
				family,
				RefreshTokenCodec.hash(plaintext),
				now.plus(authProperties.refreshTokenTtl()),
				now));
		return new IssuedRefreshToken(plaintext, entity);
	}

	private void revokeFamily(RefreshTokenFamilyEntity family, Instant now) {
		family.revoke(now);
		refreshTokens.revokeAllInFamily(family.getId(), now);
	}

	private void revokeAllForStaff(Long staffId) {
		Instant now = clock.instant();
		families.revokeAllForStaff(staffId, now);
		refreshTokens.revokeAllForStaff(staffId, now);
	}

	private LockedRefresh lockRefresh(String refreshToken) {
		String hash = RefreshTokenCodec.hash(refreshToken);
		Long staffId = refreshTokens.findStaffIdByTokenHash(hash).orElseThrow(IdentityException::unauthorized);
		StaffAccountEntity staff = staffAccounts.findByIdForUpdate(staffId).orElseThrow(IdentityException::unauthorized);
		RefreshTokenEntity token = refreshTokens.findByTokenHashForUpdate(hash).orElseThrow(IdentityException::unauthorized);
		return new LockedRefresh(staff, token);
	}

	private record IssuedRefreshToken(String plaintext, RefreshTokenEntity entity) {
	}

	private record LockedRefresh(StaffAccountEntity staff, RefreshTokenEntity token) {
	}
}
