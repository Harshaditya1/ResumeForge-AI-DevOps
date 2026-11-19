package com.resumeforge.application;

import com.resumeforge.application.dto.*;
import com.resumeforge.auth.CurrentUserService;
import com.resumeforge.auth.user.User;
import com.resumeforge.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
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
        User user = currentUserService.getCurrentUser();
        Long userId = user.getId();

        // Duplicate prevention check
        String comp = request.getCompanyName().trim();
        String title = request.getJobTitle().trim();
        String url = request.getApplicationUrl() != null ? request.getApplicationUrl().trim() : "";
        String extId = request.getExternalJobId() != null ? request.getExternalJobId().trim() : "";

        if (!extId.isBlank() && repository.existsByUserIdAndExternalJobId(userId, extId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already saved this job opportunity.");
        }
        if (!url.isBlank() && repository.existsByUserIdAndApplicationUrl(userId, url)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already saved an application with this application URL.");
        }
        if (repository.existsByUserIdAndCompanyNameIgnoreCaseAndJobTitleIgnoreCase(userId, comp, title)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already saved an application for " + title + " at " + comp + ".");
        }

        JobApplication application = new JobApplication();
        application.setUser(user);
        copy(request, application);
        return toResponse(repository.save(application));
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponseDto> getAll(String search, JobApplicationStatus status) {
        Long userId = currentUserService.getCurrentUser().getId();
        List<JobApplication> applications = repository.findAllByUserIdOrderByUpdatedAtDesc(userId);

        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        return applications.stream()
                .filter(app -> status == null || app.getStatus() == status)
                .filter(app -> {
                    if (normalizedSearch.isBlank()) return true;
                    boolean matchCompany = app.getCompanyName() != null && app.getCompanyName().toLowerCase(Locale.ROOT).contains(normalizedSearch);
                    boolean matchTitle = app.getJobTitle() != null && app.getJobTitle().toLowerCase(Locale.ROOT).contains(normalizedSearch);
                    boolean matchLocation = app.getLocation() != null && app.getLocation().toLowerCase(Locale.ROOT).contains(normalizedSearch);
                    boolean matchNotes = app.getNotes() != null && app.getNotes().toLowerCase(Locale.ROOT).contains(normalizedSearch);
                    return matchCompany || matchTitle || matchLocation || matchNotes;
                })
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
        application.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        application.setExperienceLevel(request.getExperienceLevel() != null ? request.getExperienceLevel().trim() : null);
        application.setJobType(request.getJobType() != null ? request.getJobType().trim() : null);
        application.setSource(request.getSource() != null ? request.getSource().trim() : null);
        application.setJobDescription(request.getJobDescription());
        application.setApplicationUrl(request.getApplicationUrl() != null ? request.getApplicationUrl().trim() : null);
        application.setAppliedDate(request.getAppliedDate());
        application.setInterviewDate(request.getInterviewDate());
        application.setDeadline(request.getDeadline());
        application.setExternalJobId(request.getExternalJobId() != null ? request.getExternalJobId().trim() : null);
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
                .location(application.getLocation())
                .experienceLevel(application.getExperienceLevel())
                .jobType(application.getJobType())
                .source(application.getSource())
                .jobDescription(application.getJobDescription())
                .applicationUrl(application.getApplicationUrl())
                .appliedDate(application.getAppliedDate())
                .interviewDate(application.getInterviewDate())
                .deadline(application.getDeadline())
                .externalJobId(application.getExternalJobId())
                .status(application.getStatus())
                .notes(application.getNotes())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
