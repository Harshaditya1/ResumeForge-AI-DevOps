package com.resumeforge.jobdiscovery;

import com.resumeforge.application.JobApplicationService;
import com.resumeforge.application.JobApplicationStatus;
import com.resumeforge.application.dto.JobApplicationRequestDto;
import com.resumeforge.application.dto.JobApplicationResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobDiscoveryController {

    private final JobDiscoveryService discoveryService;
    private final JobApplicationService applicationService;

    public JobDiscoveryController(
            JobDiscoveryService discoveryService,
            JobApplicationService applicationService
    ) {
        this.discoveryService = discoveryService;
        this.applicationService = applicationService;
    }

    @GetMapping("/discover")
    public List<DiscoveredJobDto> discoverJobs(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String search
    ) {
        return discoveryService.discoverJobs(role, location, jobType, search);
    }

    @PostMapping("/save-discovered")
    public ResponseEntity<JobApplicationResponseDto> saveDiscovered(
            @RequestBody DiscoveredJobDto job
    ) {
        JobApplicationRequestDto request = new JobApplicationRequestDto();
        request.setCompanyName(job.getCompanyName());
        request.setJobTitle(job.getJobTitle());
        request.setLocation(job.getLocation());
        request.setExperienceLevel(job.getExperienceLevel());
        request.setJobType(job.getJobType());
        request.setSource(job.getSource());
        request.setJobDescription(job.getDescription());
        request.setApplicationUrl(job.getApplicationUrl());
        request.setExternalJobId(job.getId());
        request.setStatus(JobApplicationStatus.SAVED);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(applicationService.create(request));
    }
}

