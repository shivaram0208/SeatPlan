package com.seatplan.repository;

import com.seatplan.model.SeatAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatAssignmentRepository extends JpaRepository<SeatAssignment, Long> {
    List<SeatAssignment> findByExamId(Long examId);
    void deleteByExamId(Long examId);
    void deleteByHallId(Long hallId);
    void deleteByStudentId(Long studentId);
}
