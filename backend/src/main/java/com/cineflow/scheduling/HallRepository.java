package com.cineflow.scheduling;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface HallRepository extends JpaRepository<HallEntity, Long> {

	@Query("select h from HallEntity h left join fetch h.seats where h.id = :id")
	Optional<HallEntity> findByIdWithSeats(long id);

	List<HallEntity> findAllByOrderByNameAsc();
}
