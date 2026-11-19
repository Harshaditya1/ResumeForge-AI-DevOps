package com.resumeforge.resumehealth;

import com.resumeforge.analysis.Analysis;
import com.resumeforge.analysis.AnalysisRepository;
import com.resumeforge.analysis.KeywordExtractorService;
import com.resumeforge.auth.CurrentUserService;
import com.resumeforge.auth.user.User;
import com.resumeforge.exception.ResourceNotFoundException;
import com.resumeforge.resume.Resume;
import com.resumeforge.resume.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeHealthService {

    private final ResumeRepository resumeRepository;
    private final CurrentUserService currentUserService;
    private final ResumeHealthChecklistRepository checklistRepository;
    private final AnalysisRepository analysisRepository;
    private final KeywordExtractorService keywordExtractorService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}|(?:\\+?91[-.\\s]?)?[6-9]\\d{9}"
    );

    public ResumeHealthService(
            ResumeRepository resumeRepository,
            CurrentUserService currentUserService,
            ResumeHealthChecklistRepository checklistRepository,
            AnalysisRepository analysisRepository,
            KeywordExtractorService keywordExtractorService
    ) {
        this.resumeRepository = resumeRepository;
        this.currentUserService = currentUserService;
        this.checklistRepository = checklistRepository;
        this.analysisRepository = analysisRepository;
        this.keywordExtractorService = keywordExtractorService;
    }

    @Transactional(readOnly = true)
    public ResumeHealthResponseDto getLatest() {
        Long userId = currentUserService.getCurrentUser().getId();
        Resume resume = resumeRepository
                .findFirstByUserIdOrderByUploadedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No resume uploaded."
                ));
        return calculate(resume, userId);
    }

    @Transactional(readOnly = true)
    public ResumeHealthResponseDto getById(Long resumeId) {
        Long userId = currentUserService.getCurrentUser().getId();
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume not found."
                ));
        return calculate(resume, userId);
    }

    @Transactional
    public ResumeHealthResponseDto updateChecklist(
            ResumeHealthChecklistRequestDto request
    ) {
        User user = currentUserService.getCurrentUser();
        Resume resume = resumeRepository.findByIdAndUserId(
                        request.getResumeId(),
                        user.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resume not found."
                ));
        String key = request.getSectionKey().trim();
        ResumeHealthChecklist item = checklistRepository
                .findByUserIdAndResumeIdAndSectionKey(
                        user.getId(),
                        resume.getId(),
                        key
                )
                .orElseGet(() -> ResumeHealthChecklist.builder()
                        .user(user)
                        .resume(resume)
                        .sectionKey(key)
                        .build());
        item.setCompleted(request.isCompleted());
        checklistRepository.save(item);
        return calculate(resume, user.getId());
    }

    private ResumeHealthResponseDto calculate(Resume resume, Long userId) {
        String rawText = resume.getExtractedText() != null ? resume.getExtractedText().trim() : "";
        String lowerText = rawText.toLowerCase(Locale.ROOT);

        Map<String, Boolean> checklist = checklistRepository
                .findAllByUserIdAndResumeId(userId, resume.getId())
                .stream()
                .collect(Collectors.toMap(
                        ResumeHealthChecklist::getSectionKey,
                        ResumeHealthChecklist::isCompleted
                ));

        // 1. Extract Actual Contact Info (Email, Phone, Candidate Name)
        String extractedEmail = extractEmail(rawText);
        String extractedPhone = extractPhone(rawText);
        String candidateName = extractCandidateName(rawText);

        // 2. Extract Technical Skills (Real skills detected in resume)
        Set<String> detectedSkillsSet = keywordExtractorService.filterKnownSkills(
                keywordExtractorService.extractKeywords(rawText)
        );
        List<String> technicalSkills = new ArrayList<>(detectedSkillsSet);
        Collections.sort(technicalSkills);

        // 3. Extract Section Previews
        String educationPreview = extractSectionSnippet(rawText, "education", "academic", "degree", "bachelor", "b.tech");
        String experiencePreview = extractSectionSnippet(rawText, "experience", "work history", "employment", "internship");
        String projectsPreview = extractSectionSnippet(rawText, "projects", "academic projects", "key projects");

        // 4. Section completeness checks
        boolean hasContact = extractedEmail != null || extractedPhone != null;
        boolean hasEducation = !educationPreview.isBlank() || containsAny(lowerText, "education", "university", "college", "b.tech", "bachelor", "degree");
        boolean hasExperience = !experiencePreview.isBlank() || containsAny(lowerText, "experience", "employment", "internship", "work history");
        boolean hasProjects = !projectsPreview.isBlank() || containsAny(lowerText, "project", "github", "developed", "built");
        boolean hasSkills = technicalSkills.size() >= 3;
        boolean hasFormatting = rawText.length() >= 250 && !rawText.lines().filter(l -> !l.isBlank()).toList().isEmpty();

        List<ResumeHealthSectionDto> sections = List.of(
                ResumeHealthSectionDto.builder()
                        .key("contact")
                        .label("Contact Information")
                        .complete(hasContact)
                        .weight(15)
                        .recommendation(hasContact
                                ? "Contact details found (" + (extractedEmail != null ? extractedEmail : "phone only") + ")."
                                : "Add a clear professional email address and reachable phone number.")
                        .checklistCompleted(checklist.getOrDefault("contact", false))
                        .build(),
                ResumeHealthSectionDto.builder()
                        .key("education")
                        .label("Education")
                        .complete(hasEducation)
                        .weight(15)
                        .recommendation(hasEducation
                                ? "Education credentials detected."
                                : "Add your degree, institution, branch of study, and graduation year.")
                        .checklistCompleted(checklist.getOrDefault("education", false))
                        .build(),
                ResumeHealthSectionDto.builder()
                        .key("experience")
                        .label("Work & Internship Experience")
                        .complete(hasExperience)
                        .weight(20)
                        .recommendation(hasExperience
                                ? "Work/internship experience detected."
                                : "Add relevant internships, freelance work, or practical industry experience.")
                        .checklistCompleted(checklist.getOrDefault("experience", false))
                        .build(),
                ResumeHealthSectionDto.builder()
                        .key("projects")
                        .label("Projects & Portfolio")
                        .complete(hasProjects)
                        .weight(15)
                        .recommendation(hasProjects
                                ? "Projects section detected."
                                : "Add 2-3 substantive software projects highlighting tools used and measurable outcomes.")
                        .checklistCompleted(checklist.getOrDefault("projects", false))
                        .build(),
                ResumeHealthSectionDto.builder()
                        .key("technical-skills")
                        .label("Technical Skills")
                        .complete(hasSkills)
                        .weight(20)
                        .recommendation(hasSkills
                                ? technicalSkills.size() + " technical skills identified."
                                : "List recognized technical skills (e.g. Java, Spring Boot, React, SQL, Git).")
                        .checklistCompleted(checklist.getOrDefault("technical-skills", false))
                        .build(),
                ResumeHealthSectionDto.builder()
                        .key("formatting")
                        .label("ATS Readability & Structure")
                        .complete(hasFormatting)
                        .weight(15)
                        .recommendation(hasFormatting
                                ? "Resume contains clean, parseable text sections."
                                : "Ensure your PDF contains selectable, parseable text rather than scanned images.")
                        .checklistCompleted(checklist.getOrDefault("formatting", false))
                        .build()
        );

        int totalScore = sections.stream()
                .filter(ResumeHealthSectionDto::isComplete)
                .mapToInt(ResumeHealthSectionDto::getWeight)
                .sum();

        List<String> missing = sections.stream()
                .filter(s -> !s.isComplete())
                .map(ResumeHealthSectionDto::getLabel)
                .toList();

        // 5. Actionable Recommendations
        List<String> recommendations = new ArrayList<>();
        if (!hasContact) {
            recommendations.add("Add a header with your full name, email address, phone number, and LinkedIn/GitHub URLs.");
        }
        if (!hasEducation) {
            recommendations.add("Create an Education section listing your university, degree, and graduation timeline.");
        }
        if (!hasExperience) {
            recommendations.add("Highlight internship experiences, open-source work, or academic assistantships with quantifiable metrics.");
        }
        if (!hasProjects) {
            recommendations.add("Feature key personal or academic projects with GitHub repositories and live demo links.");
        }
        if (technicalSkills.size() < 5) {
            recommendations.add("Expand your technical skills list to include core programming languages, frameworks, and databases.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Your resume covers all fundamental sections! Ensure bullet points use strong action verbs and metrics.");
        }

        // 6. Query Latest ATS Analysis if available
        Optional<Analysis> latestAnalysisOpt = analysisRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
        boolean hasAts = latestAnalysisOpt.isPresent();
        Double atsScore = null;
        String atsScoreCategory = null;
        String atsOverallAssessment = null;
        List<String> atsMissingKeywords = Collections.emptyList();
        List<String> atsStrengths = Collections.emptyList();
        List<String> atsWeakAreas = Collections.emptyList();

        if (hasAts) {
            Analysis analysis = latestAnalysisOpt.get();
            atsScore = analysis.getMatchPercentage();
            atsScoreCategory = analysis.getScoreCategory();
            atsOverallAssessment = analysis.getOverallAssessment();
            atsMissingKeywords = analysis.getMissingKeywords();
            atsStrengths = analysis.getStrengths();
            atsWeakAreas = analysis.getImprovements();
        }

        return ResumeHealthResponseDto.builder()
                .resumeId(resume.getId())
                .fileName(resume.getOriginalFileName())
                .score(totalScore)
                .completedSections((int) sections.stream().filter(ResumeHealthSectionDto::isComplete).count())
                .totalSections(sections.size())
                .candidateName(candidateName != null ? candidateName : "Not Found")
                .email(extractedEmail != null ? extractedEmail : "Not Found")
                .phone(extractedPhone != null ? extractedPhone : "Not Found")
                .educationPreview(!educationPreview.isBlank() ? educationPreview : "Not Found")
                .experiencePreview(!experiencePreview.isBlank() ? experiencePreview : "Not Found")
                .projectsPreview(!projectsPreview.isBlank() ? projectsPreview : "Not Found")
                .technicalSkills(technicalSkills)
                .hasAtsAnalysis(hasAts)
                .atsScore(atsScore)
                .atsScoreCategory(atsScoreCategory)
                .atsOverallAssessment(atsOverallAssessment)
                .atsMissingKeywords(atsMissingKeywords)
                .atsStrengths(atsStrengths)
                .atsWeakAreas(atsWeakAreas)
                .sections(sections)
                .missingSections(missing)
                .improvementRecommendations(recommendations)
                .checklistCompleted((int) sections.stream().filter(ResumeHealthSectionDto::isChecklistCompleted).count())
                .checklistTotal(sections.size())
                .build();
    }

    private static String extractEmail(String text) {
        if (text == null) return null;
        Matcher m = EMAIL_PATTERN.matcher(text);
        if (m.find()) {
            return m.group().trim();
        }
        return null;
    }

    private static String extractPhone(String text) {
        if (text == null) return null;
        Matcher m = PHONE_PATTERN.matcher(text);
        if (m.find()) {
            return m.group().trim();
        }
        return null;
    }

    private String extractCandidateName(String text) {
        if (text == null || text.isBlank()) return null;
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < Math.min(6, lines.length); i++) {
            String line = lines[i].trim();
            if (line.isBlank()) continue;
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.contains("resume") || lower.contains("curriculum") || lower.contains("vitae") || lower.contains("@") || lower.contains("http")) {
                continue;
            }
            // Check if line looks like a person's name (between 2 and 4 words, letters only)
            if (line.matches("^[A-Za-z]+(?:\\s+[A-Za-z]+){1,3}$") && line.length() < 50) {
                return line;
            }
        }
        // Fallback to logged-in user's name
        User user = currentUserService.getCurrentUser();
        if (user != null && user.getFullName() != null) {
            return user.getFullName();
        }
        return null;
    }

    private static String extractSectionSnippet(String text, String... headers) {
        if (text == null || text.isBlank()) return "";
        String[] lines = text.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim().toLowerCase(Locale.ROOT);
            for (String h : headers) {
                if (trimmed.equals(h) || trimmed.startsWith(h + ":") || trimmed.startsWith(h + " -") || trimmed.endsWith(" " + h)) {
                    // Extract next 2-3 non-blank lines as snippet
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    for (int j = i + 1; j < lines.length && count < 3; j++) {
                        String next = lines[j].trim();
                        if (next.isBlank()) continue;
                        if (isLikelyHeader(next)) break;
                        if (sb.length() > 0) sb.append(" • ");
                        sb.append(next);
                        count++;
                    }
                    if (sb.length() > 180) {
                        return sb.substring(0, 180) + "...";
                    }
                    return sb.toString();
                }
            }
        }
        return "";
    }

    private static boolean isLikelyHeader(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return lower.equals("education") || lower.equals("experience") || lower.equals("projects")
                || lower.equals("skills") || lower.equals("certifications") || lower.equals("summary")
                || lower.equals("work experience");
    }

    private static boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
