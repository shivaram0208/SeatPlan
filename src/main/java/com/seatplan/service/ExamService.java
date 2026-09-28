package com.seatplan.service;

import com.seatplan.exception.BusinessException;
import com.seatplan.exception.ResourceNotFoundException;
import com.seatplan.model.ExamSession;
import com.seatplan.repository.ExamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExamService {
    private final ExamRepository repository;
    private final com.seatplan.repository.SeatAssignmentRepository assignmentRepository;

    public ExamService(ExamRepository repository,
                       com.seatplan.repository.SeatAssignmentRepository assignmentRepository) {
        this.repository = repository;
        this.assignmentRepository = assignmentRepository;
    }

    public List<ExamSession> findAll() {
        return repository.findAll();
    }

    public ExamSession findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
    }

    @Transactional
    public ExamSession create(ExamSession exam) {
        validate(exam);
        if (repository.existsByNameIgnoreCase(exam.getName().trim())) {
            throw new BusinessException("Exam name already exists.");
        }
        exam.setName(exam.getName().trim());
        return repository.save(exam);
    }

    @Transactional
    public ExamSession update(Long id, ExamSession input) {
        ExamSession existing = findById(id);
        validate(input);
        if (!existing.getName().equalsIgnoreCase(input.getName().trim())
                && repository.existsByNameIgnoreCase(input.getName().trim())) {
            throw new BusinessException("Exam name already exists.");
        }
        existing.setName(input.getName().trim());
        existing.setExamDate(input.getExamDate());
        existing.setStartTime(input.getStartTime());
        existing.setEndTime(input.getEndTime());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        ExamSession exam = findById(id);
        assignmentRepository.deleteByExamId(exam.getId());
        repository.delete(exam);
    }

    private void validate(ExamSession exam) {
        if (exam.getStartTime() != null && exam.getEndTime() != null
                && !exam.getEndTime().isAfter(exam.getStartTime())) {
            throw new BusinessException("End time must be after start time.");
        }
    }
}
