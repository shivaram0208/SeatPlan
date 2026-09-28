package com.seatplan.controller;

import com.seatplan.dto.GenerateSeatingRequest;
import com.seatplan.dto.SeatingPlanDto;
import com.seatplan.service.SeatingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seating")
public class SeatingController {
    private final SeatingService service;

    public SeatingController(SeatingService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public SeatingPlanDto generate(@Valid @RequestBody GenerateSeatingRequest request) {
        return service.generate(request);
    }

    @GetMapping("/exam/{examId}")
    public SeatingPlanDto get(@PathVariable Long examId) {
        return service.getPlan(examId);
    }

    @DeleteMapping("/exam/{examId}")
    public void delete(@PathVariable Long examId) {
        service.deletePlan(examId);
    }
}
