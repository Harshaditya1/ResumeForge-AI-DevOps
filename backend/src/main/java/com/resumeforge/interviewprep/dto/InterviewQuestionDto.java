package com.resumeforge.interviewprep.dto;

import com.resumeforge.interviewprep.*;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InterviewQuestionDto {
    Long id;
    InterviewQuestionCategory category;
    InterviewQuestionDifficulty difficulty;
    String question;
    String answer;
    boolean completed;
    boolean favorite;
}
