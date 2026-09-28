package com.seatplan.service;

import com.seatplan.exception.BusinessException;
import com.seatplan.exception.ResourceNotFoundException;
import com.seatplan.model.Hall;
import com.seatplan.repository.HallRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HallService {
    private final HallRepository repository;
    private final com.seatplan.repository.SeatAssignmentRepository assignmentRepository;

    public HallService(HallRepository repository,
                       com.seatplan.repository.SeatAssignmentRepository assignmentRepository) {
        this.repository = repository;
        this.assignmentRepository = assignmentRepository;
    }

    public List<Hall> findAll() {
        return repository.findAll();
    }

    public Hall findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found: " + id));
    }

    @Transactional
    public Hall create(Hall hall) {
        if (repository.existsByNameIgnoreCase(hall.getName().trim())) {
            throw new BusinessException("Hall name already exists.");
        }
        validateSize(hall);
        hall.setName(hall.getName().trim());
        return repository.save(hall);
    }

    @Transactional
    public Hall update(Long id, Hall input) {
        Hall existing = findById(id);
        if (!existing.getName().equalsIgnoreCase(input.getName().trim())
                && repository.existsByNameIgnoreCase(input.getName().trim())) {
            throw new BusinessException("Hall name already exists.");
        }
        validateSize(input);
        existing.setName(input.getName().trim());
        existing.setRows(input.getRows());
        existing.setColumns(input.getColumns());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Hall hall = findById(id);
        assignmentRepository.deleteByHallId(hall.getId());
        repository.delete(hall);
    }

    private void validateSize(Hall hall) {
        if (hall.getRows() < 1 || hall.getColumns() < 1) {
            throw new BusinessException("Rows and columns must be positive.");
        }
        if ((long) hall.getRows() * hall.getColumns() > 10000) {
            throw new BusinessException("Hall capacity cannot exceed 10,000 seats.");
        }
    }
}
