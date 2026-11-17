package com.resumeforge.resumehealth;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ResumeHealthResponseDto {
    Long resumeId;
    String fileName;
    int score;
    int completedSections;
    int totalSections;
    List<ResumeHealthSectionDto> sections;
    List<String> missingSections;
    int checklistCompleted;
    int checklistTotal;
}
