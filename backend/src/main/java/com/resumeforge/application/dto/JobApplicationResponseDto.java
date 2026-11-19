package com.resumeforge.application.dto;

import com.resumeforge.application.JobApplicationStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class JobApplicationResponseDto {
    Long id;
    String companyName;
    String jobTitle;
    String location;
    String experienceLevel;
    String jobType;
    String source;
    String jobDescription;
    String applicationUrl;
    LocalDate appliedDate;
    LocalDate interviewDate;
    LocalDate deadline;
    String externalJobId;
    JobApplicationStatus status;
    String notes;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
