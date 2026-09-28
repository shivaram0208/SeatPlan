package com.seatplan.controller;

import com.seatplan.repository.ExamRepository;
import com.seatplan.repository.HallRepository;
import com.seatplan.repository.SeatAssignmentRepository;
import com.seatplan.repository.StudentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DashboardController {
    private final StudentRepository students;
    private final HallRepository halls;
    private final ExamRepository exams;
    private final SeatAssignmentRepository assignments;

    public DashboardController(StudentRepository students, HallRepository halls,
                               ExamRepository exams, SeatAssignmentRepository assignments) {
        this.students = students;
        this.halls = halls;
        this.exams = exams;
        this.assignments = assignments;
    }

    @GetMapping("/api/dashboard")
    public Map<String, Object> dashboard() {
        return Map.of(
                "students", students.count(),
                "halls", halls.count(),
                "exams", exams.count(),
                "generatedAssignments", assignments.count()
        );
    }
}
