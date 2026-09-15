package com.cineflow.scheduling;

import org.springframework.data.jpa.repository.JpaRepository;

interface ShowtimeRepository extends JpaRepository<ShowtimeEntity, Long> {
}
