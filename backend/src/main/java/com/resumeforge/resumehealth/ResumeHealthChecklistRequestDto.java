package com.resumeforge.resumehealth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResumeHealthChecklistRequestDto {

    @NotNull
    private Long resumeId;

    @NotBlank
    private String sectionKey;

    private boolean completed;
}
