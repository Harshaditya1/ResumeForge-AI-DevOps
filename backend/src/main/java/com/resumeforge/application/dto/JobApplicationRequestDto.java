package com.resumeforge.application.dto;

import com.resumeforge.application.JobApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JobApplicationRequestDto {

    @NotBlank(message = "Company name is required")
    @Size(max = 160, message = "Company name must be 160 characters or fewer")
    private String companyName;

    @NotBlank(message = "Job title is required")
    @Size(max = 160, message = "Job title must be 160 characters or fewer")
    private String jobTitle;

    @Size(max = 160, message = "Location must be 160 characters or fewer")
    private String location;

    @Size(max = 60, message = "Experience level must be 60 characters or fewer")
    private String experienceLevel;

    @Size(max = 60, message = "Job type must be 60 characters or fewer")
    private String jobType;

    @Size(max = 100, message = "Source must be 100 characters or fewer")
    private String source;

    private String jobDescription;

    @Size(max = 1000, message = "Application URL must be 1000 characters or fewer")
    private String applicationUrl;

    private LocalDate appliedDate;

    private LocalDate interviewDate;

    private LocalDate deadline;

    @Size(max = 100, message = "External job ID must be 100 characters or fewer")
    private String externalJobId;

    private JobApplicationStatus status;

    private String notes;
}
