package com.resumeforge.resumehealth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeHealthSectionDto {
    String key;
    String label;
    boolean complete;
    int weight;
    String recommendation;
    boolean checklistCompleted;
}
