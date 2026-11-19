package com.resumeforge.interviewprep.dto;

import com.resumeforge.interviewprep.InterviewQuestionCategory;
import com.resumeforge.interviewprep.InterviewQuestionDifficulty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InterviewQuestionDto {
    Long id;
    InterviewQuestionCategory category;
    InterviewQuestionDifficulty difficulty;
    String title;
    String topic;
    String question;
    String answer;
    String sourceName;
    String sourceUrl;
    boolean completed;
    boolean favorite;
}
