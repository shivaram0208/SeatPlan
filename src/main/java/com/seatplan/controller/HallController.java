package com.seatplan.controller;

import com.seatplan.model.Hall;
import com.seatplan.service.HallService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
public class HallController {
    private final HallService service;

    public HallController(HallService service) {
        this.service = service;
    }

    @GetMapping
    public List<Hall> all() { return service.findAll(); }

    @GetMapping("/{id}")
    public Hall one(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Hall create(@Valid @RequestBody Hall hall) { return service.create(hall); }

    @PutMapping("/{id}")
    public Hall update(@PathVariable Long id, @Valid @RequestBody Hall hall) {
        return service.update(id, hall);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
