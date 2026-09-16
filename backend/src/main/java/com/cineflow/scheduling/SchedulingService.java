package com.cineflow.scheduling;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineflow.audit.Audit;
import com.cineflow.audit.AuditAction;
import com.cineflow.catalog.Catalog;
import com.cineflow.catalog.MovieForSchedule;

@Service
class SchedulingService implements Scheduling {

	private static final String SHOWTIME_SELECT = """
			select s.id, s.movie_id, m.title, m.runtime_minutes, s.hall_id, h.name,
			       s.starts_at, s.adult_price_myr, s.child_price_myr
			from cineflow.showtimes s
			join cineflow.movies m on m.id = s.movie_id
			join cineflow.halls h on h.id = s.hall_id
			""";

	private final HallRepository halls;
	private final SeatRepository seats;
	private final ShowtimeRepository showtimes;
	private final Catalog catalog;
	private final JdbcTemplate jdbcTemplate;
	private final Audit audit;
	private final Clock clock;

	SchedulingService(
			HallRepository halls,
			SeatRepository seats,
			ShowtimeRepository showtimes,
			Catalog catalog,
			JdbcTemplate jdbcTemplate,
			Audit audit,
			Clock clock) {
		this.halls = halls;
		this.seats = seats;
		this.showtimes = showtimes;
		this.catalog = catalog;
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
		if (jdbcTemplate.queryForList("select id from cineflow.halls where id = ? for update", Long.class, hallId)
				.isEmpty()) {
			throw SchedulingException.hallNotFound();
		}
		HallEntity hall = halls.findByIdWithSeats(hallId).orElseThrow(SchedulingException::hallNotFound);
		if (hall.getArchivedAt() == null) {
			hall.archive(clock.instant());
			audit.record(actorStaffId, AuditAction.HALL_ARCHIVED, "hall", Long.toString(hall.getId()));
		}
		return hall.toResponse();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ShowtimeResponse> listShowtimes() {
		return jdbcTemplate.query(SHOWTIME_SELECT + " order by s.starts_at, s.id", this::mapShowtime);
	}

	@Override
	@Transactional
	public ShowtimeResponse createShowtime(
			long actorStaffId,
			long movieId,
			long hallId,
			String startsAtLocal,
			String timeZone,
			BigDecimal adultPriceMyr,
			BigDecimal childPriceMyr) {
		Instant startsAt = CinemaTime.toInstant(startsAtLocal, timeZone);
		requirePositivePrices(adultPriceMyr, childPriceMyr);
		HallEntity hall = halls.findById(hallId).orElseThrow(SchedulingException::hallNotFound);
		if (hall.getArchivedAt() != null) {
			throw SchedulingException.hallArchived();
		}
		MovieForSchedule movie = catalog.findMovieForSchedule(movieId).orElseThrow(SchedulingException::movieNotFound);
		if (movie.archived()) {
			throw SchedulingException.movieArchived();
		}
		ShowtimeEntity saved;
		try {
			saved = showtimes.saveAndFlush(new ShowtimeEntity(hallId, movieId, startsAt, adultPriceMyr, childPriceMyr));
		}
		catch (DataIntegrityViolationException exception) {
			throw mapShowtimeWriteFailure(exception);
		}
		audit.record(actorStaffId, AuditAction.SHOWTIME_CREATED, "showtime", Long.toString(saved.getId()));
		return requireResponse(saved.getId());
	}

	@Override
	@Transactional
	public ShowtimeResponse updateShowtimePrices(
			long actorStaffId,
			long showtimeId,
			BigDecimal adultPriceMyr,
			BigDecimal childPriceMyr) {
		requirePositivePrices(adultPriceMyr, childPriceMyr);
		ShowtimeEntity showtime = showtimes.findById(showtimeId).orElseThrow(SchedulingException::showtimeNotFound);
		showtime.setPrices(adultPriceMyr, childPriceMyr);
		showtimes.flush();
		audit.record(actorStaffId, AuditAction.SHOWTIME_PRICES_UPDATED, "showtime", Long.toString(showtime.getId()));
		return requireResponse(showtime.getId());
	}

	@Override
	@Transactional
	public void removeShowtime(long actorStaffId, long showtimeId) {
		if (jdbcTemplate.queryForList("select id from cineflow.showtimes where id = ? for update", Long.class, showtimeId)
				.isEmpty()) {
			throw SchedulingException.showtimeNotFound();
		}
		ShowtimeEntity showtime = showtimes.findById(showtimeId).orElseThrow(SchedulingException::showtimeNotFound);
		Instant now = clock.instant();
		if (!showtime.getStartsAt().isAfter(now)) {
			throw SchedulingException.showtimeNotRemovable();
		}
		if (hasBookings(showtimeId)) {
			throw SchedulingException.showtimeHasBookings();
		}
		jdbcTemplate.update(
				"""
						delete from cineflow.seat_claims
						where showtime_id = ? and claim_kind = 'HOLD' and expires_at <= ?
						""",
				showtimeId,
				Timestamp.from(now));
		jdbcTemplate.update(
				"""
						delete from cineflow.seat_holds
						where showtime_id = ? and expires_at <= ?
						""",
				showtimeId,
				Timestamp.from(now));
		if (hasActiveHolds(showtimeId)) {
			throw SchedulingException.showtimeNotRemovable();
		}
		try {
			showtimes.delete(showtime);
			showtimes.flush();
		}
		catch (DataIntegrityViolationException exception) {
			throw mapShowtimeWriteFailure(exception);
		}
		audit.record(actorStaffId, AuditAction.SHOWTIME_REMOVED, "showtime", Long.toString(showtimeId));
	}

	private boolean seatNotDisableable(long seatId) {
		Boolean blocked = jdbcTemplate.queryForObject(
				"select cineflow.seat_not_disableable(?)", Boolean.class, seatId);
		return Boolean.TRUE.equals(blocked);
	}

	private boolean hasBookings(long showtimeId) {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from cineflow.seat_claims where showtime_id = ? and claim_kind = 'BOOKING'",
				Integer.class,
				showtimeId);
		return count != null && count > 0;
	}

	private boolean hasActiveHolds(long showtimeId) {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from cineflow.seat_claims where showtime_id = ? and claim_kind = 'HOLD'",
				Integer.class,
				showtimeId);
		return count != null && count > 0;
	}

	private ShowtimeResponse requireResponse(long showtimeId) {
		List<ShowtimeResponse> found = jdbcTemplate.query(
				SHOWTIME_SELECT + " where s.id = ?", this::mapShowtime, showtimeId);
		if (found.isEmpty()) {
			throw SchedulingException.showtimeNotFound();
		}
		return found.getFirst();
	}

	private ShowtimeResponse mapShowtime(ResultSet resultSet, int rowNum) throws SQLException {
		return toResponse(
				resultSet.getLong("id"),
				resultSet.getLong("movie_id"),
				resultSet.getString("title"),
				resultSet.getInt("runtime_minutes"),
				resultSet.getLong("hall_id"),
				resultSet.getString("name"),
				resultSet.getTimestamp("starts_at").toInstant(),
				resultSet.getBigDecimal("adult_price_myr"),
				resultSet.getBigDecimal("child_price_myr"));
	}

	private static void requirePositivePrices(BigDecimal adultPriceMyr, BigDecimal childPriceMyr) {
		if (adultPriceMyr == null || childPriceMyr == null
				|| adultPriceMyr.signum() <= 0 || childPriceMyr.signum() <= 0) {
			throw SchedulingException.invalidPrice();
		}
	}

	private static SchedulingException mapShowtimeWriteFailure(DataIntegrityViolationException exception) {
		String message = String.valueOf(exception.getMostSpecificCause().getMessage());
		if (message.contains("showtimes_hall_occupancy_excl")) {
			return SchedulingException.showtimeOverlap();
		}
		if (message.contains("scheduling.hall_archived")) {
			return SchedulingException.hallArchived();
		}
		if (message.contains("scheduling.movie_archived")) {
			return SchedulingException.movieArchived();
		}
		if (message.contains("scheduling.showtime_has_bookings")) {
			return SchedulingException.showtimeHasBookings();
		}
		if (message.contains("seat_claims_showtime_hall_fk")) {
			return SchedulingException.showtimeNotRemovable();
		}
		throw exception;
	}

	private static ShowtimeResponse toResponse(
			long id,
			long movieId,
			String movieTitle,
			int runtimeMinutes,
			long hallId,
			String hallName,
			Instant startsAt,
			BigDecimal adultPriceMyr,
			BigDecimal childPriceMyr) {
		return new ShowtimeResponse(
				id,
				movieId,
				movieTitle,
				runtimeMinutes,
				hallId,
				hallName,
				startsAt,
				CinemaTime.formatLocal(startsAt),
				CinemaTime.ZONE.getId(),
				CinemaTime.occupancyEnd(startsAt, runtimeMinutes),
				adultPriceMyr,
				childPriceMyr);
	}
}
