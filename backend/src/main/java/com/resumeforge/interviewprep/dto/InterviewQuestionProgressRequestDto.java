package com.resumeforge.interviewprep.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InterviewQuestionProgressRequestDto {

    @NotNull
    private Long questionId;

    private boolean completed;

    private boolean favorite;
}
