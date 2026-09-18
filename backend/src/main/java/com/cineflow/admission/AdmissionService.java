package com.cineflow.admission;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineflow.audit.Audit;
import com.cineflow.audit.AuditAction;
import com.cineflow.booking.TicketType;
import com.cineflow.platform.Sha256;
import com.cineflow.scheduling.CinemaTime;

@Service
class AdmissionService implements Admission {

	private static final String BOOKING_SELECT = """
			select b.id, b.booking_reference, b.showtime_id, s.starts_at,
			       m.title as movie_title, h.name as hall_name
			from cineflow.bookings b
			join cineflow.showtimes s on s.id = b.showtime_id
			join cineflow.movies m on m.id = s.movie_id
			join cineflow.halls h on h.id = s.hall_id
			""";

	private static final String BOOKING_BY_TOKEN_SELECT = BOOKING_SELECT + """
			where b.admission_token_hash = ?
			""";

	private static final String BOOKING_BY_REFERENCE_SELECT = BOOKING_SELECT + """
			where b.booking_reference = ?
			""";

	private static final String INSERT_ADMISSION = """
			insert into cineflow.admissions (booking_id, admitted_by, admitted_at)
			values (?, ?, ?)
			on conflict (booking_id) do nothing
			""";

	private static final String SEATS_SELECT = """
			select seat.row_label, seat.seat_number, claim.ticket_type
			from cineflow.seat_claims claim
			join cineflow.seats seat on seat.id = claim.seat_id
			where claim.booking_id = ?
			order by seat.row_label, seat.seat_number
			""";

	private final JdbcTemplate jdbcTemplate;
	private final Clock clock;
	private final Audit audit;

	AdmissionService(JdbcTemplate jdbcTemplate, Clock clock, Audit audit) {
		this.jdbcTemplate = jdbcTemplate;
		this.clock = clock;
		this.audit = audit;
	}

	@Override
	@Transactional
	public AdmissionResponse admit(long staffId, AdmitBookingRequest request) {
		String token = request == null ? null : normalize(request.admissionToken());
		String reference = request == null ? null : normalizeReference(request.bookingReference());
		if ((token == null) == (reference == null)) {
			throw AdmissionException.invalidRequest();
		}

		BookingRow booking = token != null ? findByAdmissionToken(token) : findByBookingReference(reference);

		Instant now = clock.instant();
		int inserted = jdbcTemplate.update(INSERT_ADMISSION, booking.id(), staffId, Timestamp.from(now));
		if (inserted == 0) {
			throw AdmissionException.alreadyAdmitted();
		}
		audit.record(staffId, AuditAction.BOOKING_ADMITTED, "BOOKING", booking.bookingReference());

		List<AdmittedSeatResponse> seats = jdbcTemplate.query(SEATS_SELECT, this::mapSeat, booking.id());
		return new AdmissionResponse(
				booking.bookingReference(),
				booking.showtimeId(),
				booking.movieTitle(),
				booking.hallName(),
				CinemaTime.formatLocal(booking.startsAt()),
				CinemaTime.ZONE.getId(),
				seats,
				now);
	}

	private BookingRow findByAdmissionToken(String token) {
		List<BookingRow> found = jdbcTemplate.query(
				BOOKING_BY_TOKEN_SELECT,
				this::mapBooking,
				Sha256.hash(token));
		if (found.isEmpty()) {
			throw AdmissionException.notFound();
		}
		return found.getFirst();
	}

	private BookingRow findByBookingReference(String reference) {
		List<BookingRow> found = jdbcTemplate.query(BOOKING_BY_REFERENCE_SELECT, this::mapBooking, reference);
		if (found.isEmpty()) {
			throw AdmissionException.notFound();
		}
		return found.getFirst();
	}

	private static String normalize(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static String normalizeReference(String value) {
		String normalized = normalize(value);
		return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
	}

	private BookingRow mapBooking(ResultSet resultSet, int rowNum) throws SQLException {
		return new BookingRow(
				resultSet.getLong("id"),
				resultSet.getString("booking_reference"),
				resultSet.getLong("showtime_id"),
				resultSet.getTimestamp("starts_at").toInstant(),
				resultSet.getString("movie_title"),
				resultSet.getString("hall_name"));
	}

	private AdmittedSeatResponse mapSeat(ResultSet resultSet, int rowNum) throws SQLException {
		String label = resultSet.getString("row_label") + resultSet.getInt("seat_number");
		return new AdmittedSeatResponse(label, TicketType.valueOf(resultSet.getString("ticket_type")));
	}

	private record BookingRow(
			long id,
			String bookingReference,
			long showtimeId,
			Instant startsAt,
			String movieTitle,
			String hallName) {
	}
}
