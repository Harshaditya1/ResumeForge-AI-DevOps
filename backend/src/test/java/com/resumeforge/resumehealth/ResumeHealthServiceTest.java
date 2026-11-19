package com.resumeforge.resumehealth;

import com.resumeforge.analysis.Analysis;
import com.resumeforge.analysis.AnalysisRepository;
import com.resumeforge.analysis.KeywordExtractorService;
import com.resumeforge.auth.CurrentUserService;
import com.resumeforge.auth.user.User;
import com.resumeforge.resume.Resume;
import com.resumeforge.resume.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeHealthServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ResumeHealthChecklistRepository checklistRepository;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private KeywordExtractorService keywordExtractorService;

    private ResumeHealthService service;
    private User testUser;
    private Resume testResume;

    @BeforeEach
    void setUp() {
        service = new ResumeHealthService(
                resumeRepository,
                currentUserService,
                checklistRepository,
                analysisRepository,
                keywordExtractorService
        );

        testUser = User.builder().id(1L).fullName("Rahul Sharma").email("rahul.sharma@example.com").build();

        String sampleResumeText = """
                Rahul Sharma
                Email: rahul.sharma@example.com | Phone: +91 9876543210
                
                EDUCATION
                B.Tech in Computer Science and Engineering
                Delhi Technological University, 2024
                
                TECHNICAL SKILLS
                Java, Spring Boot, React, PostgreSQL, Docker, Git
                
                EXPERIENCE
                Software Engineer Intern - ABC Technologies
                Developed REST APIs using Spring Boot and Hibernate.
                
                PROJECTS
                ResumeForge AI - Full stack web application with React and Spring Boot.
                """;

        testResume = Resume.builder()
                .id(50L)
                .user(testUser)
                .originalFileName("Rahul_Sharma_Resume.pdf")
                .extractedText(sampleResumeText)
                .build();
    }

    @Test
    void testGetLatestResumeHealthExtractsActualData() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(resumeRepository.findFirstByUserIdOrderByUploadedAtDesc(1L)).thenReturn(Optional.of(testResume));
        when(checklistRepository.findAllByUserIdAndResumeId(1L, 50L)).thenReturn(List.of());

        when(keywordExtractorService.extractKeywords(anyString()))
                .thenReturn(Set.of("java", "spring boot", "react", "postgresql", "docker", "git"));
        when(keywordExtractorService.filterKnownSkills(any()))
                .thenReturn(Set.of("Java", "Spring Boot", "React", "PostgreSQL", "Docker", "Git"));

        // With existing ATS analysis
        Analysis mockAnalysis = Analysis.builder()
                .id(1L)
                .matchPercentage(85.0)
                .scoreCategory("STRONG MATCH")
                .overallAssessment("High keyword match with Java developer criteria.")
                .missingKeywords(List.of("Kubernetes", "AWS"))
                .strengths(List.of("Strong Java and Spring Boot experience"))
                .improvements(List.of("Add cloud deployment details"))
                .build();
        when(analysisRepository.findFirstByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(mockAnalysis));

        ResumeHealthResponseDto health = service.getLatest();

        assertNotNull(health);
        assertEquals("Rahul_Sharma_Resume.pdf", health.getFileName());
        assertEquals("rahul.sharma@example.com", health.getEmail());
        assertTrue(health.getPhone().contains("9876543210"));
        assertTrue(health.getCandidateName().contains("Rahul Sharma"));
        assertTrue(health.getTechnicalSkills().contains("Java"));
        assertTrue(health.getTechnicalSkills().contains("Spring Boot"));

        // Completeness score should be high (all sections present)
        assertTrue(health.getScore() >= 80);

        // ATS details
        assertTrue(health.isHasAtsAnalysis());
        assertEquals(85.0, health.getAtsScore());
        assertEquals("STRONG MATCH", health.getAtsScoreCategory());
        assertFalse(health.getSections().isEmpty());
    }

    @Test
    void testUpdateChecklist() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(resumeRepository.findByIdAndUserId(50L, 1L)).thenReturn(Optional.of(testResume));
        when(checklistRepository.findByUserIdAndResumeIdAndSectionKey(1L, 50L, "education"))
                .thenReturn(Optional.empty());

        ResumeHealthChecklistRequestDto request = new ResumeHealthChecklistRequestDto();
        request.setResumeId(50L);
        request.setSectionKey("education");
        request.setCompleted(true);

        when(keywordExtractorService.extractKeywords(anyString())).thenReturn(Set.of());
        when(keywordExtractorService.filterKnownSkills(any())).thenReturn(Set.of());
        when(analysisRepository.findFirstByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());

        ResumeHealthResponseDto updated = service.updateChecklist(request);

        assertNotNull(updated);
        verify(checklistRepository, times(1)).save(any(ResumeHealthChecklist.class));
    }
}
