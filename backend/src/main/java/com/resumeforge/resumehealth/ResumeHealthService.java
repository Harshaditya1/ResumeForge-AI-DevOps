package com.resumeforge.resumehealth;

import com.resumeforge.auth.CurrentUserService;
import com.resumeforge.exception.ResourceNotFoundException;
import com.resumeforge.resume.Resume;
import com.resumeforge.resume.ResumeRepository;
import com.resumeforge.auth.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResumeHealthService {

    private final ResumeRepository resumeRepository;
    private final CurrentUserService currentUserService;
    private final ResumeHealthChecklistRepository checklistRepository;

    public ResumeHealthService(
            ResumeRepository resumeRepository,
            CurrentUserService currentUserService,
            ResumeHealthChecklistRepository checklistRepository
    ) {
        this.resumeRepository = resumeRepository;
        this.currentUserService = currentUserService;
        this.checklistRepository = checklistRepository;
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
        String text = resume.getExtractedText() == null
                ? ""
                : resume.getExtractedText().toLowerCase(Locale.ROOT);
        Map<String, Boolean> checklist = checklistRepository
                .findAllByUserIdAndResumeId(userId, resume.getId())
                .stream()
                .collect(Collectors.toMap(
                        ResumeHealthChecklist::getSectionKey,
                        ResumeHealthChecklist::isCompleted
                ));

        List<ResumeHealthSectionDto> sections = List.of(
                section(
                        "contact",
                        "Contact information",
                        15,
                        text,
                        value -> value.contains("@")
                                && value.matches("(?s).*\\+?[0-9][0-9 ()-]{7,}[0-9].*"),
                        "Add a professional email address and phone number.",
                        checklist.getOrDefault("contact", false)
                ),
                section(
                        "education",
                        "Education",
                        15,
                        text,
                        value -> containsAny(value, "education", "university", "college", "b.tech", "bachelor"),
                        "Add your degree, institution, and graduation year.",
                        checklist.getOrDefault("education", false)
                ),
                section(
                        "experience",
                        "Experience",
                        20,
                        text,
                        value -> containsAny(value, "experience", "employment", "work history"),
                        "Add relevant experience with measurable achievements.",
                        checklist.getOrDefault("experience", false)
                ),
                section(
                        "projects",
                        "Projects",
                        15,
                        text,
                        value -> value.contains("project"),
                        "Add projects with your contribution, tools, and outcomes.",
                        checklist.getOrDefault("projects", false)
                ),
                section(
                        "technical-skills",
                        "Technical skills",
                        20,
                        text,
                        value -> containsAny(value, "skills", "technologies", "technical skills"),
                        "List the technical skills used in your target roles.",
                        checklist.getOrDefault("technical-skills", false)
                ),
                section(
                        "certifications",
                        "Certifications",
                        15,
                        text,
                        value -> containsAny(value, "certification", "certified"),
                        "Add relevant certifications or remove this section if none apply.",
                        checklist.getOrDefault("certifications", false)
                )
        );

        int score = sections.stream()
                .filter(ResumeHealthSectionDto::isComplete)
                .mapToInt(ResumeHealthSectionDto::getWeight)
                .sum();

        return ResumeHealthResponseDto.builder()
                .resumeId(resume.getId())
                .fileName(resume.getOriginalFileName())
                .score(score)
                .completedSections((int) sections.stream()
                        .filter(ResumeHealthSectionDto::isComplete)
                        .count())
                .totalSections(sections.size())
                .sections(sections)
                .missingSections(sections.stream()
                        .filter(section -> !section.isComplete())
                        .map(ResumeHealthSectionDto::getLabel)
                        .toList())
                .checklistCompleted((int) sections.stream()
                        .filter(ResumeHealthSectionDto::isChecklistCompleted)
                        .count())
                .checklistTotal(sections.size())
                .build();
    }

    private ResumeHealthSectionDto section(
            String key,
            String label,
            int weight,
            String text,
            Predicate<String> matcher,
            String recommendation,
            boolean checklistCompleted
    ) {
        boolean complete = matcher.test(text);
        return ResumeHealthSectionDto.builder()
                .key(key)
                .label(label)
                .complete(complete)
                .weight(weight)
                .recommendation(complete ? "Section detected." : recommendation)
                .checklistCompleted(checklistCompleted)
                .build();
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
