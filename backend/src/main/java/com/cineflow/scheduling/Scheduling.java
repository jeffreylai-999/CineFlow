package com.cineflow.scheduling;

import java.util.List;

public interface Scheduling {

	List<HallSummaryResponse> listHalls();

	HallResponse createHall(long actorStaffId, String name, int rowCount, int seatsPerRow);

	HallResponse getHall(long hallId);

	SeatResponse setSeatDisabled(long actorStaffId, long hallId, long seatId, boolean disabled);

	HallResponse archiveHall(long actorStaffId, long hallId);
}
