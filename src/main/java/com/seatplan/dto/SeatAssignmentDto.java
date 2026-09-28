package com.seatplan.dto;

import com.seatplan.model.SeatAssignment;

public record SeatAssignmentDto(
        Long id,
        String hallName,
        Long hallId,
        int seatNumber,
        int rowNumber,
        int columnNumber,
        Long studentId,
        String registerNumber,
        String studentName,
        String classSection
) {
    public static SeatAssignmentDto from(SeatAssignment a) {
        return new SeatAssignmentDto(
                a.getId(),
                a.getHall().getName(),
                a.getHall().getId(),
                a.getSeatNumber(),
                a.getRowNumber(),
                a.getColumnNumber(),
                a.getStudent().getId(),
                a.getStudent().getRegisterNumber(),
                a.getStudent().getName(),
                a.getStudent().getClassSection()
        );
    }
}
