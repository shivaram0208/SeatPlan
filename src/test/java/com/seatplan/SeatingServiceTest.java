package com.seatplan;

import com.seatplan.dto.GenerateSeatingRequest;
import com.seatplan.dto.SeatingPlanDto;
import com.seatplan.model.ExamSession;
import com.seatplan.model.Hall;
import com.seatplan.model.Student;
import com.seatplan.repository.ExamRepository;
import com.seatplan.repository.HallRepository;
import com.seatplan.repository.SeatAssignmentRepository;
import com.seatplan.repository.StudentRepository;
import com.seatplan.service.SeatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SeatingServiceTest {
    @Autowired StudentRepository students;
    @Autowired HallRepository halls;
    @Autowired ExamRepository exams;
    @Autowired SeatAssignmentRepository assignments;
    @Autowired SeatingService seatingService;

    @Test
    void generatedPlanContainsEveryStudentAndAvoidsAdjacentSameSection() {
        students.deleteAll();
        halls.deleteAll();
        exams.deleteAll();

        for (int i = 0; i < 12; i++) {
            String section = i % 3 == 0 ? "A" : i % 3 == 1 ? "B" : "C";
            students.save(new Student("R" + i, "Student " + i, section));
        }
        Hall hall = halls.save(new Hall("Test Hall", 3, 4));
        ExamSession exam = exams.save(new ExamSession(
                "Test Exam", LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(12, 0)));

        SeatingPlanDto plan = seatingService.generate(
                new GenerateSeatingRequest(exam.getId(), List.of(hall.getId())));

        assertEquals(12, plan.assignments().size());
        assertEquals(12, plan.assignments().stream().map(x -> x.studentId()).distinct().count());

        for (var a : plan.assignments()) {
            for (var b : plan.assignments()) {
                if (a.hallId().equals(b.hallId())
                        && Math.abs(a.rowNumber() - b.rowNumber()) + Math.abs(a.columnNumber() - b.columnNumber()) == 1) {
                    assertNotEquals(a.classSection(), b.classSection());
                }
            }
        }
    }
}
