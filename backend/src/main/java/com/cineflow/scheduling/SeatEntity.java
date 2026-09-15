package com.cineflow.scheduling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats", schema = "cineflow")
class SeatEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hall_id", nullable = false)
	private HallEntity hall;

	@Column(name = "row_label", nullable = false)
	private String rowLabel;

	@Column(name = "seat_number", nullable = false)
	private int seatNumber;

	@Column(nullable = false)
	private boolean disabled;

	protected SeatEntity() {
	}

	SeatEntity(HallEntity hall, String rowLabel, int seatNumber) {
		this.hall = hall;
		this.rowLabel = rowLabel;
		this.seatNumber = seatNumber;
		this.disabled = false;
	}

	Long getId() {
		return id;
	}

	String getRowLabel() {
		return rowLabel;
	}

	int getSeatNumber() {
		return seatNumber;
	}

	boolean isDisabled() {
		return disabled;
	}

	void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	String label() {
		return rowLabel + seatNumber;
	}

	SeatResponse toResponse() {
		return new SeatResponse(id, rowLabel, seatNumber, label(), disabled);
	}
}
