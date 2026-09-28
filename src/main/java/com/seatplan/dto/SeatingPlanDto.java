package com.seatplan.dto;

import com.seatplan.model.ExamSession;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record SeatingPlanDto(
        Long examId,
        String examName,
        LocalDate examDate,
        LocalTime startTime,
        LocalTime endTime,
        int studentCount,
        List<SeatAssignmentDto> assignments
) {
    public static SeatingPlanDto of(ExamSession exam, List<SeatAssignmentDto> assignments) {
        return new SeatingPlanDto(
                exam.getId(), exam.getName(), exam.getExamDate(),
                exam.getStartTime(), exam.getEndTime(),
                assignments.size(), assignments
        );
    }
}
