package com.seatplan.repository;

import com.seatplan.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByRegisterNumberIgnoreCase(String registerNumber);
    Optional<Student> findByRegisterNumberIgnoreCase(String registerNumber);
}
