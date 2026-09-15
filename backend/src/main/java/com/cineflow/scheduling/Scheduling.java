package com.cineflow.scheduling;

import java.math.BigDecimal;
import java.util.List;

public interface Scheduling {

	List<HallSummaryResponse> listHalls();

	HallResponse createHall(long actorStaffId, String name, int rowCount, int seatsPerRow);

	HallResponse getHall(long hallId);

	SeatResponse setSeatDisabled(long actorStaffId, long hallId, long seatId, boolean disabled);

	HallResponse archiveHall(long actorStaffId, long hallId);

	List<ShowtimeResponse> listShowtimes();

	ShowtimeResponse createShowtime(
			long actorStaffId,
			long movieId,
			long hallId,
			String startsAtLocal,
			String timeZone,
			BigDecimal adultPriceMyr,
			BigDecimal childPriceMyr);

	ShowtimeResponse updateShowtimePrices(
			long actorStaffId,
			long showtimeId,
			BigDecimal adultPriceMyr,
			BigDecimal childPriceMyr);

	void removeShowtime(long actorStaffId, long showtimeId);
}
