package com.cineflow.scheduling;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface SeatRepository extends JpaRepository<SeatEntity, Long> {

	Optional<SeatEntity> findByIdAndHall_Id(long id, long hallId);
}
