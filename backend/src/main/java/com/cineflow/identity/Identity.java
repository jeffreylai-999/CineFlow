package com.cineflow.identity;

public interface Identity {

	StaffSession login(String username, String password);

	StaffSession refresh(String refreshToken);

	void logout(String refreshToken);

	void deactivate(long actorStaffId, long targetStaffId);

	void resetPassword(long actorStaffId, long targetStaffId, String newPassword);

	StaffProfile me(long staffId);
}
