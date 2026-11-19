package com.resumeforge.application;

import com.resumeforge.application.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:5173")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponseDto> create(
            @Valid @RequestBody JobApplicationRequestDto request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping
    public List<JobApplicationResponseDto> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) JobApplicationStatus status
    ) {
        return service.getAll(search, status);
    }

    @GetMapping("/statistics")
    public JobApplicationStatisticsDto getStatistics() {
        return service.getStatistics();
    }

    @GetMapping("/{id}")
    public JobApplicationResponseDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public JobApplicationResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationRequestDto request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
