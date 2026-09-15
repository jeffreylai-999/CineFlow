package com.cineflow.scheduling;

import java.time.Clock;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineflow.audit.Audit;
import com.cineflow.audit.AuditAction;

@Service
class SchedulingService implements Scheduling {

	private final HallRepository halls;
	private final SeatRepository seats;
	private final JdbcTemplate jdbcTemplate;
	private final Audit audit;
	private final Clock clock;

	SchedulingService(
			HallRepository halls,
			SeatRepository seats,
			JdbcTemplate jdbcTemplate,
			Audit audit,
			Clock clock) {
		this.halls = halls;
		this.seats = seats;
		this.jdbcTemplate = jdbcTemplate;
		this.audit = audit;
		this.clock = clock;
	}

	@Override
	@Transactional(readOnly = true)
	public List<HallSummaryResponse> listHalls() {
		return halls.findAllByOrderByNameAsc().stream().map(HallEntity::toSummary).toList();
	}

	@Override
	@Transactional
	public HallResponse createHall(long actorStaffId, String name, int rowCount, int seatsPerRow) {
		HallEntity hall = new HallEntity(name.strip(), rowCount, seatsPerRow, clock.instant());
		for (int row = 0; row < rowCount; row++) {
			String rowLabel = Character.toString('A' + row);
			for (int seatNumber = 1; seatNumber <= seatsPerRow; seatNumber++) {
				hall.addSeat(new SeatEntity(hall, rowLabel, seatNumber));
			}
		}
		HallEntity saved = halls.saveAndFlush(hall);
		saved.lockSeatMap();
		audit.record(actorStaffId, AuditAction.HALL_CREATED, "hall", Long.toString(saved.getId()));
		return saved.toResponse();
	}

	@Override
	@Transactional(readOnly = true)
	public HallResponse getHall(long hallId) {
		return halls.findByIdWithSeats(hallId)
			.map(HallEntity::toResponse)
			.orElseThrow(SchedulingException::hallNotFound);
	}

	@Override
	@Transactional
	public SeatResponse setSeatDisabled(long actorStaffId, long hallId, long seatId, boolean disabled) {
		if (!halls.existsById(hallId)) {
			throw SchedulingException.hallNotFound();
		}
		SeatEntity seat = seats.findByIdAndHall_Id(seatId, hallId).orElseThrow(SchedulingException::seatNotFound);
		if (disabled && !seat.isDisabled() && seatNotDisableable(seat.getId())) {
			throw SchedulingException.seatNotDisableable();
		}
		seat.setDisabled(disabled);
		audit.record(
				actorStaffId,
				disabled ? AuditAction.SEAT_DISABLED : AuditAction.SEAT_ENABLED,
				"seat",
				Long.toString(seat.getId()));
		return seat.toResponse();
	}

	@Override
	@Transactional
	public HallResponse archiveHall(long actorStaffId, long hallId) {
		HallEntity hall = halls.findByIdWithSeats(hallId).orElseThrow(SchedulingException::hallNotFound);
		if (hall.getArchivedAt() == null) {
			hall.archive(clock.instant());
			audit.record(actorStaffId, AuditAction.HALL_ARCHIVED, "hall", Long.toString(hall.getId()));
		}
		return hall.toResponse();
	}

	private boolean seatNotDisableable(long seatId) {
		Boolean blocked = jdbcTemplate.queryForObject(
				"select cineflow.seat_not_disableable(?)", Boolean.class, seatId);
		return Boolean.TRUE.equals(blocked);
	}
}
