package com.cineflow.identity;

import java.util.List;

public interface Identity {

	StaffSession login(String username, String password);

	StaffSession refresh(String refreshToken);

	void logout(String refreshToken);

	void deactivate(long actorStaffId, long targetStaffId);

	void resetPassword(long actorStaffId, long targetStaffId, String newPassword);

	List<StaffAccountSummary> listStaffAccounts(long actorStaffId);

	StaffAccountSummary createBookingStaff(long actorStaffId, String username, String password);

	StaffProfile me(long staffId);
}
