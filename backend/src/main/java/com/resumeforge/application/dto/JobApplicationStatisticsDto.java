package com.resumeforge.application.dto;

import com.resumeforge.application.JobApplicationStatus;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class JobApplicationStatisticsDto {
    long total;
    Map<JobApplicationStatus, Long> byStatus;
}
