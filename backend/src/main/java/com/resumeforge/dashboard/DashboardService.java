package com.resumeforge.dashboard;

import com.resumeforge.generatedresume.GeneratedResumeRepository;
import com.resumeforge.jobdescription.JobDescriptionRepository;
import com.resumeforge.resume.ResumeRepository;
import com.resumeforge.tailoring.TailoredResumeRepository;
import com.resumeforge.application.JobApplicationRepository;
import com.resumeforge.auth.CurrentUserService;
import com.resumeforge.interviewprep.UserQuestionProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ResumeRepository resumeRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final TailoredResumeRepository tailoredResumeRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserQuestionProgressRepository userQuestionProgressRepository;
    private final CurrentUserService currentUserService;

    public DashboardSummaryDTO getDashboardSummary() {
        Long userId = currentUserService.getCurrentUser().getId();

        return DashboardSummaryDTO.builder()
                .totalResumes(resumeRepository.countByUserId(userId))
                .totalGeneratedResumes(generatedResumeRepository.count())
                .totalJobDescriptions(jobDescriptionRepository.countByUserId(userId))
                .totalTailoredResumes(tailoredResumeRepository.count())
                .totalApplications(jobApplicationRepository.countByUserId(userId))
                .totalInterviewQuestionsCompleted(
                        userQuestionProgressRepository.countByUserIdAndCompletedTrue(userId)
                )
                .build();
    }
}
