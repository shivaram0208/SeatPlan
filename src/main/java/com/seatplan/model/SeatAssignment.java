package com.seatplan.model;

import jakarta.persistence.*;

@Entity
@Table(name = "seat_assignments",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_exam_seat", columnNames = {"exam_id", "hall_id", "seat_number"}),
           @UniqueConstraint(name = "uk_exam_student", columnNames = {"exam_id", "student_id"})
       })
public class SeatAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private ExamSession exam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Column(name = "column_number", nullable = false)
    private int columnNumber;

    public SeatAssignment() {}

    public SeatAssignment(ExamSession exam, Hall hall, Student student,
                          int seatNumber, int rowNumber, int columnNumber) {
        this.exam = exam;
        this.hall = hall;
        this.student = student;
        this.seatNumber = seatNumber;
        this.rowNumber = rowNumber;
        this.columnNumber = columnNumber;
    }

    public Long getId() { return id; }
    public ExamSession getExam() { return exam; }
    public Hall getHall() { return hall; }
    public Student getStudent() { return student; }
    public int getSeatNumber() { return seatNumber; }
    public int getRowNumber() { return rowNumber; }
    public int getColumnNumber() { return columnNumber; }

    public void setId(Long id) { this.id = id; }
    public void setExam(ExamSession exam) { this.exam = exam; }
    public void setHall(Hall hall) { this.hall = hall; }
    public void setStudent(Student student) { this.student = student; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
    public void setColumnNumber(int columnNumber) { this.columnNumber = columnNumber; }
}
