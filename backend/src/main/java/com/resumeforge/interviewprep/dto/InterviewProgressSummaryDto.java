package com.resumeforge.interviewprep.dto;

import lombok.Builder;
import lombok.Value;
import java.util.Map;
import com.resumeforge.interviewprep.InterviewQuestionCategory;

@Value
@Builder
public class InterviewProgressSummaryDto {
    long totalQuestions;
    long completedQuestions;
    long favoriteQuestions;
    Map<InterviewQuestionCategory, InterviewCategoryProgressDto> byCategory;
}
