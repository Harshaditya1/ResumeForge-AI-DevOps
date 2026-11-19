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

    // Actual candidate details extracted from resume
    String candidateName;
    String email;
    String phone;
    String educationPreview;
    String experiencePreview;
    String projectsPreview;
    List<String> technicalSkills;

    // ATS analysis details from latest analysis if available
    boolean hasAtsAnalysis;
    Double atsScore;
    String atsScoreCategory;
    String atsOverallAssessment;
    List<String> atsMissingKeywords;
    List<String> atsStrengths;
    List<String> atsWeakAreas;

    // Health sections & Actionable recommendations
    List<ResumeHealthSectionDto> sections;
    List<String> missingSections;
    List<String> improvementRecommendations;

    // Interactive Checklist
    int checklistCompleted;
    int checklistTotal;
}
