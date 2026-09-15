package com.cineflow.identity;

public record StaffSessionResponse(String accessToken, long expiresInSeconds, StaffProfile staff) {

	static StaffSessionResponse from(StaffSession session) {
		return new StaffSessionResponse(
				session.accessToken(),
				session.accessTokenTtl().toSeconds(),
				session.staff());
	}
}
