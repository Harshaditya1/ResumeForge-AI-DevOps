package com.resumeforge.application;

import com.resumeforge.application.dto.*;
import com.resumeforge.auth.CurrentUserService;
import com.resumeforge.auth.user.User;
import com.resumeforge.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final CurrentUserService currentUserService;

    public JobApplicationService(
            JobApplicationRepository repository,
            CurrentUserService currentUserService
    ) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public JobApplicationResponseDto create(JobApplicationRequestDto request) {
        JobApplication application = new JobApplication();
        application.setUser(currentUserService.getCurrentUser());
        copy(request, application);
        return toResponse(repository.save(application));
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponseDto> getAll() {
        Long userId = currentUserService.getCurrentUser().getId();
        return repository.findAllByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobApplicationResponseDto getById(Long id) {
        return toResponse(findOwned(id));
    }

    @Transactional
    public JobApplicationResponseDto update(
            Long id,
            JobApplicationRequestDto request
    ) {
        JobApplication application = findOwned(id);
        copy(request, application);
        return toResponse(repository.save(application));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findOwned(id));
    }

    @Transactional(readOnly = true)
    public JobApplicationStatisticsDto getStatistics() {
        Long userId = currentUserService.getCurrentUser().getId();
        Map<JobApplicationStatus, Long> byStatus =
                new EnumMap<>(JobApplicationStatus.class);

        Arrays.stream(JobApplicationStatus.values())
                .forEach(status -> byStatus.put(
                        status,
                        repository.countByUserIdAndStatus(userId, status)
                ));

        return JobApplicationStatisticsDto.builder()
                .total(repository.countByUserId(userId))
                .byStatus(byStatus)
                .build();
    }

    private JobApplication findOwned(Long id) {
        User user = currentUserService.getCurrentUser();
        return repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job application not found."
                ));
    }

    private void copy(
            JobApplicationRequestDto request,
            JobApplication application
    ) {
        application.setCompanyName(request.getCompanyName().trim());
        application.setJobTitle(request.getJobTitle().trim());
        application.setJobDescription(request.getJobDescription());
        application.setApplicationUrl(request.getApplicationUrl());
        application.setAppliedDate(request.getAppliedDate());
        application.setInterviewDate(request.getInterviewDate());
        application.setStatus(
                request.getStatus() == null
                        ? JobApplicationStatus.SAVED
                        : request.getStatus()
        );
        application.setNotes(request.getNotes());
    }

    private JobApplicationResponseDto toResponse(JobApplication application) {
        return JobApplicationResponseDto.builder()
                .id(application.getId())
                .companyName(application.getCompanyName())
                .jobTitle(application.getJobTitle())
                .jobDescription(application.getJobDescription())
                .applicationUrl(application.getApplicationUrl())
                .appliedDate(application.getAppliedDate())
                .interviewDate(application.getInterviewDate())
                .status(application.getStatus())
                .notes(application.getNotes())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
