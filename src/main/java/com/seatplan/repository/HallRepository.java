package com.seatplan.repository;

import com.seatplan.model.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HallRepository extends JpaRepository<Hall, Long> {
    boolean existsByNameIgnoreCase(String name);
}
