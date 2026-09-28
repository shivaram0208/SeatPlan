package com.seatplan.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "students", uniqueConstraints = @UniqueConstraint(name = "uk_student_reg_no", columnNames = "register_number"))
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 30)
    @Column(name = "register_number", nullable = false, length = 30)
    private String registerNumber;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Size(max = 40)
    @Column(name = "class_section", nullable = false, length = 40)
    private String classSection;

    public Student() {}

    public Student(String registerNumber, String name, String classSection) {
        this.registerNumber = registerNumber;
        this.name = name;
        this.classSection = classSection;
    }

    public Long getId() { return id; }
    public String getRegisterNumber() { return registerNumber; }
    public String getName() { return name; }
    public String getClassSection() { return classSection; }

    public void setId(Long id) { this.id = id; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }
    public void setName(String name) { this.name = name; }
    public void setClassSection(String classSection) { this.classSection = classSection; }
}
