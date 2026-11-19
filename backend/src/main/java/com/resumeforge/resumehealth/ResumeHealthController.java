package com.resumeforge.resumehealth;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/resume-health")
public class ResumeHealthController {

    private final ResumeHealthService service;

    public ResumeHealthController(ResumeHealthService service) {
        this.service = service;
    }

    @GetMapping("/latest")
    public ResumeHealthResponseDto getLatest() {
        return service.getLatest();
    }

    @GetMapping("/{resumeId}")
    public ResumeHealthResponseDto getById(@PathVariable Long resumeId) {
        return service.getById(resumeId);
    }

    @PutMapping("/checklist")
    public ResumeHealthResponseDto updateChecklist(
            @Valid @RequestBody ResumeHealthChecklistRequestDto request
    ) {
        return service.updateChecklist(request);
    }
}
