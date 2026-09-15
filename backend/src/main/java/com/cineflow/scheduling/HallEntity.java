package com.cineflow.scheduling;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "halls", schema = "cineflow")
class HallEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "row_count", nullable = false)
	private int rowCount;

	@Column(name = "seats_per_row", nullable = false)
	private int seatsPerRow;

	@Column(name = "seat_map_locked", nullable = false)
	private boolean seatMapLocked;

	@Column(name = "archived_at")
	private Instant archivedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@OneToMany(mappedBy = "hall", cascade = CascadeType.ALL)
	@OrderBy("rowLabel ASC, seatNumber ASC")
	private List<SeatEntity> seats = new ArrayList<>();

	protected HallEntity() {
	}

	HallEntity(String name, int rowCount, int seatsPerRow, Instant createdAt) {
		this.name = name;
		this.rowCount = rowCount;
		this.seatsPerRow = seatsPerRow;
		this.seatMapLocked = false;
		this.createdAt = createdAt;
	}

	Long getId() {
		return id;
	}

	String getName() {
		return name;
	}

	int getRowCount() {
		return rowCount;
	}

	int getSeatsPerRow() {
		return seatsPerRow;
	}

	Instant getArchivedAt() {
		return archivedAt;
	}

	List<SeatEntity> getSeats() {
		return seats;
	}

	void addSeat(SeatEntity seat) {
		seats.add(seat);
	}

	void lockSeatMap() {
		this.seatMapLocked = true;
	}

	void archive(Instant archivedAt) {
		if (this.archivedAt == null) {
			this.archivedAt = archivedAt;
		}
	}

	HallSummaryResponse toSummary() {
		return new HallSummaryResponse(id, name, rowCount, seatsPerRow, archivedAt);
	}

	HallResponse toResponse() {
		return new HallResponse(
				id,
				name,
				rowCount,
				seatsPerRow,
				archivedAt,
				seats.stream().map(SeatEntity::toResponse).toList());
	}
}
