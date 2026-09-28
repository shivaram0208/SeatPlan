package com.seatplan.controller;

import com.seatplan.model.ExamSession;
import com.seatplan.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
    private final ExamService service;

    public ExamController(ExamService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExamSession> all() { return service.findAll(); }

    @GetMapping("/{id}")
    public ExamSession one(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExamSession create(@Valid @RequestBody ExamSession exam) { return service.create(exam); }

    @PutMapping("/{id}")
    public ExamSession update(@PathVariable Long id, @Valid @RequestBody ExamSession exam) {
        return service.update(id, exam);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
