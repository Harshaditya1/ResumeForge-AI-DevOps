package com.resumeforge.interviewprep.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InterviewCategoryProgressDto {
    long totalQuestions;
    long completedQuestions;
}
