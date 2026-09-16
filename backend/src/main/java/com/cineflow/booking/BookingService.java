package com.cineflow.booking;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cineflow.scheduling.CinemaTime;

@Service
class BookingService implements Booking {

	private static final String CATALOG_SELECT = """
			select m.id, m.title, m.synopsis, m.genre, m.runtime_minutes, m.age_rating, m.poster_url,
			       s.id as showtime_id, s.starts_at, s.adult_price_myr, s.child_price_myr, h.name as hall_name
			from cineflow.movies m
			join cineflow.showtimes s on s.movie_id = m.id
			join cineflow.halls h on h.id = s.hall_id
			where m.archived_at is null
			  and s.starts_at > (cast(? as timestamptz) - make_interval(mins => m.runtime_minutes))
			order by m.title, s.starts_at, s.id
			""";

	private static final String SHOWTIME_SELECT = """
			select s.id, s.movie_id, m.title, m.runtime_minutes, s.hall_id, h.name as hall_name,
			       s.starts_at, s.adult_price_myr, s.child_price_myr
			from cineflow.showtimes s
			join cineflow.movies m on m.id = s.movie_id
			join cineflow.halls h on h.id = s.hall_id
			""";

	private static final String SHOWTIME_BY_ID_SELECT = SHOWTIME_SELECT + """
			where s.id = ?
			  and m.archived_at is null
			""";

	private static final String SHOWTIME_FOR_HOLD_SELECT = SHOWTIME_SELECT + """
			where s.id = ?
			  and m.archived_at is null
			for update of s
			""";

	private static final String HOLD_FOR_UPDATE_SELECT = """
			select id, expires_at
			from cineflow.seat_holds
			where id = ? and showtime_id = ?
			for update
			""";

	private static final String HELD_SEATS_SELECT = """
			select claim.seat_id, seat.row_label, seat.seat_number
			from cineflow.seat_claims claim
			join cineflow.seats seat on seat.id = claim.seat_id
			where claim.hold_id = ?
			order by seat.row_label, seat.seat_number
			""";

	private static final String REPLAY_SELECT = """
			select b.id, b.showtime_id, b.email, b.booking_reference,
			       s.starts_at, m.title as movie_title, h.name as hall_name, p.amount_myr
			from cineflow.payments p
			join cineflow.bookings b on b.id = p.booking_id
			join cineflow.showtimes s on s.id = b.showtime_id
			join cineflow.movies m on m.id = s.movie_id
			join cineflow.halls h on h.id = s.hall_id
			where p.idempotency_key = ?
			""";

	private static final String BOOKED_SEATS_SELECT = """
			select claim.seat_id, seat.row_label, seat.seat_number, claim.ticket_type, claim.price_myr
			from cineflow.seat_claims claim
			join cineflow.seats seat on seat.id = claim.seat_id
			where claim.booking_id = ?
			order by seat.row_label, seat.seat_number
			""";

	private static final String INSERT_BOOKING = """
			insert into cineflow.bookings (showtime_id, email, booking_reference, admission_token_hash, created_at)
			values (?, ?, ?, ?, ?)
			returning id
			""";

	private static final String CONVERT_CLAIM = """
			update cineflow.seat_claims
			set claim_kind = 'BOOKING', expires_at = null, hold_id = null,
			    booking_id = ?, ticket_type = ?, price_myr = ?
			where showtime_id = ? and seat_id = ? and claim_kind = 'HOLD' and hold_id = ?
			""";

	private static final String INSERT_PAYMENT = """
			insert into cineflow.payments (booking_id, amount_myr, method, idempotency_key, created_at)
			values (?, ?, 'CARD_SIMULATED', ?, ?)
			""";

	private static final String SEATS_SELECT = """
			select seat.id, seat.row_label, seat.seat_number,
			       not (
			           seat.disabled
			           or exists (
			               select 1
			               from cineflow.seat_claims claim
			               where claim.seat_id = seat.id
			                 and claim.showtime_id = ?
			                 and (
			                     claim.claim_kind = 'BOOKING'
			                     or (claim.claim_kind = 'HOLD' and claim.expires_at > ?)
			                 )
			           )
			       ) as available
			from cineflow.seats seat
			where seat.hall_id = ?
			order by seat.row_label, seat.seat_number
			""";

	private final JdbcTemplate jdbcTemplate;
	private final Clock clock;
	private final SeatAvailabilityPublisher availabilityPublisher;
	private final SimulatedCardPayments cardPayments;
	private final BookingReferences bookingReferences;
	private final AdmissionTokens admissionTokens;

	BookingService(
			JdbcTemplate jdbcTemplate,
			Clock clock,
			SeatAvailabilityPublisher availabilityPublisher,
			SimulatedCardPayments cardPayments,
			BookingReferences bookingReferences,
			AdmissionTokens admissionTokens) {
		this.jdbcTemplate = jdbcTemplate;
		this.clock = clock;
		this.availabilityPublisher = availabilityPublisher;
		this.cardPayments = cardPayments;
		this.bookingReferences = bookingReferences;
		this.admissionTokens = admissionTokens;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomerMovieResponse> listCatalog() {
		Instant now = clock.instant();
		List<CatalogRow> rows = jdbcTemplate.query(CATALOG_SELECT, this::mapCatalogRow, Timestamp.from(now));
		Map<Long, MovieAccumulator> movies = new LinkedHashMap<>();
		for (CatalogRow row : rows) {
			MovieAccumulator movie = movies.computeIfAbsent(row.movieId(), id -> new MovieAccumulator(row));
			movie.addShowtime(row, now);
		}
		return movies.values().stream().map(MovieAccumulator::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerSeatMapResponse showtimeSeats(long showtimeId) {
		Instant now = clock.instant();
		List<ShowtimeRow> found = jdbcTemplate.query(SHOWTIME_BY_ID_SELECT, this::mapShowtimeRow, showtimeId);
		if (found.isEmpty()) {
			throw BookingException.showtimeNotFound();
		}
		ShowtimeRow showtime = found.getFirst();
		if (!CinemaTime.stillScreening(showtime.startsAt(), showtime.runtimeMinutes(), now)) {
			throw BookingException.showtimeNotFound();
		}
		if (!CinemaTime.onlineCheckoutOpen(showtime.startsAt(), now)) {
			throw BookingException.cutoff();
		}
		int bookingLimit = bookingLimit();
		List<CustomerSeatResponse> seats = jdbcTemplate.query(
				SEATS_SELECT,
				this::mapSeat,
				showtimeId,
				Timestamp.from(now),
				showtime.hallId());
		return new CustomerSeatMapResponse(
				showtime.id(),
				showtime.movieId(),
				showtime.movieTitle(),
				showtime.hallName(),
				CinemaTime.formatLocal(showtime.startsAt()),
				CinemaTime.ZONE.getId(),
				showtime.adultPriceMyr(),
				showtime.childPriceMyr(),
				bookingLimit,
				seats);
	}

	@Override
	@Transactional
	public SeatHoldResponse createSeatHold(long showtimeId, List<Long> seatIds) {
		ShowtimeRow showtime = requireShowtimeForHold(showtimeId);
		Instant now = clock.instant();
		if (!CinemaTime.stillScreening(showtime.startsAt(), showtime.runtimeMinutes(), now)) {
			throw BookingException.showtimeNotFound();
		}
		if (!CinemaTime.onlineCheckoutOpen(showtime.startsAt(), now)) {
			throw BookingException.cutoff();
		}

		List<Long> requestedSeats = requireDistinctSeatIds(seatIds);
		if (requestedSeats.size() > bookingLimit()) {
			throw BookingException.bookingLimit();
		}

		List<SeatForHold> seats = lockRequestedSeats(showtime.hallId(), requestedSeats);
		if (seats.size() != requestedSeats.size() || seats.stream().anyMatch(SeatForHold::disabled)) {
			throw BookingException.seatsUnavailable();
		}

		Instant expiresAt = now.plusSeconds(600);
		deleteExpiredClaims(showtimeId, Timestamp.from(now), seats);
		if (hasClaimedSeats(showtimeId, seats)) {
			throw BookingException.seatsUnavailable();
		}

		UUID holdId = UUID.randomUUID();
		jdbcTemplate.update(
				"insert into cineflow.seat_holds (id, showtime_id, expires_at) values (?, ?, ?)",
				holdId,
				showtimeId,
				Timestamp.from(expiresAt));
		for (SeatForHold seat : seats) {
			jdbcTemplate.update(
					"""
							insert into cineflow.seat_claims (
							    showtime_id, hall_id, seat_id, claim_kind, expires_at, hold_id)
							values (?, ?, ?, 'HOLD', ?, ?)
							""",
					showtimeId,
					showtime.hallId(),
					seat.id(),
					Timestamp.from(expiresAt),
					holdId);
		}

		availabilityPublisher.publishAfterCommit(showtimeId);
		return new SeatHoldResponse(
				holdId,
				showtimeId,
				seats.stream().map(SeatForHold::id).toList(),
				now,
				expiresAt);
	}

	@Override
	@Transactional
	public CheckoutResult checkout(long showtimeId, CheckoutRequest request) {
		Instant now = clock.instant();
		ShowtimeRow showtime = requireShowtimeForHold(showtimeId);
		if (!CinemaTime.stillScreening(showtime.startsAt(), showtime.runtimeMinutes(), now)) {
			throw BookingException.showtimeNotFound();
		}

		Optional<CheckoutResult> replay = findReplay(request.idempotencyKey());
		if (replay.isPresent()) {
			return replay.get();
		}

		HoldRow hold = requireHoldForUpdate(request.holdId(), showtimeId);
		if (!hold.expiresAt().isAfter(now)) {
			throw BookingException.holdExpired();
		}
		List<HeldSeat> heldSeats = jdbcTemplate.query(HELD_SEATS_SELECT, this::mapHeldSeat, hold.id());
		if (heldSeats.isEmpty()) {
			throw BookingException.holdUnavailable();
		}

		Map<Long, TicketType> tickets = requireMatchingTickets(request.tickets(), heldSeats);
		Map<Long, BigDecimal> prices = new HashMap<>();
		BigDecimal total = BigDecimal.ZERO;
		for (HeldSeat seat : heldSeats) {
			BigDecimal price = tickets.get(seat.seatId()) == TicketType.ADULT
					? showtime.adultPriceMyr()
					: showtime.childPriceMyr();
			prices.put(seat.seatId(), price);
			total = total.add(price);
		}

		if (!cardPayments.approve(request.cardNumber())) {
			throw BookingException.paymentDeclined();
		}

		String email = request.email().trim().toLowerCase(Locale.ROOT);
		String bookingReference = uniqueBookingReference();
		String admissionToken = admissionTokens.newToken();
		Long bookingId = jdbcTemplate.queryForObject(
				INSERT_BOOKING,
				Long.class,
				showtimeId,
				email,
				bookingReference,
				admissionTokens.hash(admissionToken),
				Timestamp.from(now));
		for (HeldSeat seat : heldSeats) {
			jdbcTemplate.update(
					CONVERT_CLAIM,
					bookingId,
					tickets.get(seat.seatId()).name(),
					prices.get(seat.seatId()),
					showtimeId,
					seat.seatId(),
					hold.id());
		}
		jdbcTemplate.update(INSERT_PAYMENT, bookingId, total, request.idempotencyKey(), Timestamp.from(now));

		availabilityPublisher.publishAfterCommit(showtimeId);

		List<BookedSeatResponse> bookedSeats = heldSeats.stream()
			.map(seat -> new BookedSeatResponse(
					seat.seatId(),
					seat.label(),
					tickets.get(seat.seatId()),
					prices.get(seat.seatId())))
			.toList();
		return new CheckoutResult(
				new BookingConfirmationResponse(
						bookingReference,
						showtimeId,
						showtime.movieTitle(),
						showtime.hallName(),
						CinemaTime.formatLocal(showtime.startsAt()),
						CinemaTime.ZONE.getId(),
						email,
						bookedSeats,
						total,
						admissionToken),
				false);
	}

	private Optional<CheckoutResult> findReplay(String idempotencyKey) {
		List<ReplayRow> rows = jdbcTemplate.query(REPLAY_SELECT, this::mapReplayRow, idempotencyKey);
		if (rows.isEmpty()) {
			return Optional.empty();
		}
		ReplayRow row = rows.getFirst();
		List<BookedSeatResponse> seats = jdbcTemplate.query(BOOKED_SEATS_SELECT, this::mapBookedSeat, row.bookingId());
		return Optional.of(new CheckoutResult(
				new BookingConfirmationResponse(
						row.bookingReference(),
						row.showtimeId(),
						row.movieTitle(),
						row.hallName(),
						CinemaTime.formatLocal(row.startsAt()),
						CinemaTime.ZONE.getId(),
						row.email(),
						seats,
						row.amountMyr(),
						null),
				true));
	}

	private HoldRow requireHoldForUpdate(UUID holdId, long showtimeId) {
		List<HoldRow> found = jdbcTemplate.query(
				HOLD_FOR_UPDATE_SELECT,
				(resultSet, rowNum) -> new HoldRow(
						resultSet.getObject("id", UUID.class),
						resultSet.getTimestamp("expires_at").toInstant()),
				holdId,
				showtimeId);
		if (found.isEmpty()) {
			throw BookingException.holdNotFound();
		}
		return found.getFirst();
	}

	private static Map<Long, TicketType> requireMatchingTickets(
			List<CheckoutTicketRequest> tickets,
			List<HeldSeat> heldSeats) {
		Map<Long, TicketType> bySeat = new HashMap<>();
		for (CheckoutTicketRequest ticket : tickets) {
			if (ticket.seatId() == null
					|| ticket.ticketType() == null
					|| bySeat.put(ticket.seatId(), ticket.ticketType()) != null) {
				throw BookingException.invalidSeatSelection();
			}
		}
		Set<Long> heldSeatIds = new HashSet<>();
		for (HeldSeat seat : heldSeats) {
			heldSeatIds.add(seat.seatId());
		}
		if (!bySeat.keySet().equals(heldSeatIds)) {
			throw BookingException.invalidSeatSelection();
		}
		return bySeat;
	}

	private String uniqueBookingReference() {
		for (int attempt = 0; attempt < 5; attempt++) {
			String reference = bookingReferences.generate();
			Integer count = jdbcTemplate.queryForObject(
					"select count(*) from cineflow.bookings where booking_reference = ?",
					Integer.class,
					reference);
			if (count != null && count == 0) {
				return reference;
			}
		}
		throw new IllegalStateException("Unable to allocate a unique Booking Reference");
	}

	private HeldSeat mapHeldSeat(ResultSet resultSet, int rowNum) throws SQLException {
		return new HeldSeat(
				resultSet.getLong("seat_id"),
				seatLabel(resultSet.getString("row_label"), resultSet.getInt("seat_number")));
	}

	private BookedSeatResponse mapBookedSeat(ResultSet resultSet, int rowNum) throws SQLException {
		return new BookedSeatResponse(
				resultSet.getLong("seat_id"),
				seatLabel(resultSet.getString("row_label"), resultSet.getInt("seat_number")),
				TicketType.valueOf(resultSet.getString("ticket_type")),
				resultSet.getBigDecimal("price_myr"));
	}

	private static String seatLabel(String rowLabel, int seatNumber) {
		return rowLabel + seatNumber;
	}

	private ReplayRow mapReplayRow(ResultSet resultSet, int rowNum) throws SQLException {
		return new ReplayRow(
				resultSet.getLong("id"),
				resultSet.getLong("showtime_id"),
				resultSet.getString("email"),
				resultSet.getString("booking_reference"),
				resultSet.getTimestamp("starts_at").toInstant(),
				resultSet.getString("movie_title"),
				resultSet.getString("hall_name"),
				resultSet.getBigDecimal("amount_myr"));
	}

	private int bookingLimit() {
		Integer limit = jdbcTemplate.queryForObject(
				"select booking_limit from cineflow.cinema_settings where id = 1",
				Integer.class);
		return limit == null ? 10 : limit;
	}

	private ShowtimeRow requireShowtimeForHold(long showtimeId) {
		List<ShowtimeRow> found = jdbcTemplate.query(SHOWTIME_FOR_HOLD_SELECT, this::mapShowtimeRow, showtimeId);
		if (found.isEmpty()) {
			throw BookingException.showtimeNotFound();
		}
		return found.getFirst();
	}

	private static List<Long> requireDistinctSeatIds(List<Long> seatIds) {
		if (seatIds == null || seatIds.isEmpty() || seatIds.stream().anyMatch(id -> id == null || id <= 0)) {
			throw BookingException.invalidSeatSelection();
		}
		Set<Long> distinct = new HashSet<>(seatIds);
		if (distinct.size() != seatIds.size()) {
			throw BookingException.invalidSeatSelection();
		}
		return List.copyOf(seatIds);
	}

	private List<SeatForHold> lockRequestedSeats(long hallId, List<Long> seatIds) {
		String placeholders = String.join(",", Collections.nCopies(seatIds.size(), "?"));
		Object[] parameters = new Object[seatIds.size() + 1];
		parameters[0] = hallId;
		for (int index = 0; index < seatIds.size(); index++) {
			parameters[index + 1] = seatIds.get(index);
		}
		return jdbcTemplate.query(
				"""
						select id, disabled
						from cineflow.seats
						where hall_id = ? and id in (%s)
						order by row_label, seat_number, id
						for update
						""".formatted(placeholders),
				(resultSet, rowNum) -> new SeatForHold(resultSet.getLong("id"), resultSet.getBoolean("disabled")),
				parameters);
	}

	private void deleteExpiredClaims(long showtimeId, Timestamp now, List<SeatForHold> seats) {
		jdbcTemplate.update(
				"""
						delete from cineflow.seat_claims
						where showtime_id = ?
						  and claim_kind = 'HOLD'
						  and expires_at <= ?
						  and seat_id in (%s)
						""".formatted(placeholders(seats.size())),
				claimParameters(showtimeId, now, seats));
	}

	private boolean hasClaimedSeats(long showtimeId, List<SeatForHold> seats) {
		Integer count = jdbcTemplate.queryForObject(
				"""
						select count(*)
						from cineflow.seat_claims
						where showtime_id = ? and seat_id in (%s)
						""".formatted(placeholders(seats.size())),
				Integer.class,
				claimParameters(showtimeId, null, seats));
		return count != null && count > 0;
	}

	private static String placeholders(int count) {
		return String.join(",", Collections.nCopies(count, "?"));
	}

	private static Object[] claimParameters(long showtimeId, Timestamp now, List<SeatForHold> seats) {
		int offset = now == null ? 1 : 2;
		Object[] parameters = new Object[seats.size() + offset];
		parameters[0] = showtimeId;
		if (now != null) {
			parameters[1] = now;
		}
		for (int index = 0; index < seats.size(); index++) {
			parameters[index + offset] = seats.get(index).id();
		}
		return parameters;
	}

	private CatalogRow mapCatalogRow(ResultSet resultSet, int rowNum) throws SQLException {
		return new CatalogRow(
				resultSet.getLong("id"),
				resultSet.getString("title"),
				resultSet.getString("synopsis"),
				resultSet.getString("genre"),
				resultSet.getInt("runtime_minutes"),
				resultSet.getString("age_rating"),
				resultSet.getString("poster_url"),
				resultSet.getLong("showtime_id"),
				resultSet.getString("hall_name"),
				resultSet.getTimestamp("starts_at").toInstant(),
				resultSet.getBigDecimal("adult_price_myr"),
				resultSet.getBigDecimal("child_price_myr"));
	}

	private ShowtimeRow mapShowtimeRow(ResultSet resultSet, int rowNum) throws SQLException {
		return new ShowtimeRow(
				resultSet.getLong("id"),
				resultSet.getLong("movie_id"),
				resultSet.getString("title"),
				resultSet.getInt("runtime_minutes"),
				resultSet.getLong("hall_id"),
				resultSet.getString("hall_name"),
				resultSet.getTimestamp("starts_at").toInstant(),
				resultSet.getBigDecimal("adult_price_myr"),
				resultSet.getBigDecimal("child_price_myr"));
	}

	private CustomerSeatResponse mapSeat(ResultSet resultSet, int rowNum) throws SQLException {
		String rowLabel = resultSet.getString("row_label");
		int seatNumber = resultSet.getInt("seat_number");
		return new CustomerSeatResponse(
				resultSet.getLong("id"),
				rowLabel,
				seatNumber,
				seatLabel(rowLabel, seatNumber),
				resultSet.getBoolean("available"));
	}

	private record CatalogRow(
			long movieId,
			String title,
			String synopsis,
			String genre,
			int runtimeMinutes,
			String ageRating,
			String posterUrl,
			long showtimeId,
			String hallName,
			Instant startsAt,
			BigDecimal adultPriceMyr,
			BigDecimal childPriceMyr) {
	}

	private record ShowtimeRow(
			long id,
			long movieId,
			String movieTitle,
			int runtimeMinutes,
			long hallId,
			String hallName,
			Instant startsAt,
			BigDecimal adultPriceMyr,
			BigDecimal childPriceMyr) {
	}

	private record SeatForHold(long id, boolean disabled) {
	}

	private record HoldRow(UUID id, Instant expiresAt) {
	}

	private record HeldSeat(long seatId, String label) {
	}

	private record ReplayRow(
			long bookingId,
			long showtimeId,
			String email,
			String bookingReference,
			Instant startsAt,
			String movieTitle,
			String hallName,
			BigDecimal amountMyr) {
	}

	private static final class MovieAccumulator {

		private final long id;
		private final String title;
		private final String synopsis;
		private final String genre;
		private final int runtimeMinutes;
		private final String ageRating;
		private final String posterUrl;
		private final Map<String, List<CustomerShowtimeSummary>> dates = new LinkedHashMap<>();

		private MovieAccumulator(CatalogRow row) {
			this.id = row.movieId();
			this.title = row.title();
			this.synopsis = row.synopsis();
			this.genre = row.genre();
			this.runtimeMinutes = row.runtimeMinutes();
			this.ageRating = row.ageRating();
			this.posterUrl = row.posterUrl();
		}

		private void addShowtime(CatalogRow row, Instant now) {
			String date = CinemaTime.cinemaDate(row.startsAt());
			dates.computeIfAbsent(date, key -> new ArrayList<>())
				.add(new CustomerShowtimeSummary(
						row.showtimeId(),
						row.hallName(),
						CinemaTime.formatLocal(row.startsAt()),
						CinemaTime.ZONE.getId(),
						row.adultPriceMyr(),
						row.childPriceMyr(),
						CinemaTime.onlineCheckoutOpen(row.startsAt(), now)));
		}

		private CustomerMovieResponse toResponse() {
			List<CustomerShowtimeDateGroup> groups = dates.entrySet().stream()
				.map(entry -> new CustomerShowtimeDateGroup(entry.getKey(), List.copyOf(entry.getValue())))
				.toList();
			return new CustomerMovieResponse(
					id, title, synopsis, genre, runtimeMinutes, ageRating, posterUrl, groups);
		}
	}
}
