package com.seatplan.service;

import com.seatplan.exception.BusinessException;
import com.seatplan.exception.ResourceNotFoundException;
import com.seatplan.model.Student;
import com.seatplan.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository repository;
    private final com.seatplan.repository.SeatAssignmentRepository assignmentRepository;

    public StudentService(StudentRepository repository,
                          com.seatplan.repository.SeatAssignmentRepository assignmentRepository) {
        this.repository = repository;
        this.assignmentRepository = assignmentRepository;
    }

    public List<Student> findAll() {
        return repository.findAll();
    }

    public Student findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    @Transactional
    public Student create(Student student) {
        if (repository.existsByRegisterNumberIgnoreCase(student.getRegisterNumber().trim())) {
            throw new BusinessException("Register number already exists.");
        }
        student.setRegisterNumber(student.getRegisterNumber().trim());
        student.setName(student.getName().trim());
        student.setClassSection(student.getClassSection().trim());
        return repository.save(student);
    }

    @Transactional
    public Student update(Long id, Student input) {
        Student existing = findById(id);
        repository.findByRegisterNumberIgnoreCase(input.getRegisterNumber().trim())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new BusinessException("Register number already exists."); });

        existing.setRegisterNumber(input.getRegisterNumber().trim());
        existing.setName(input.getName().trim());
        existing.setClassSection(input.getClassSection().trim());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Student student = findById(id);
        assignmentRepository.deleteByStudentId(student.getId());
        repository.delete(student);
    }
}
