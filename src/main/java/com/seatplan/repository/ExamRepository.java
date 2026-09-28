package com.seatplan.repository;

import com.seatplan.model.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<ExamSession, Long> {
    boolean existsByNameIgnoreCase(String name);
}
