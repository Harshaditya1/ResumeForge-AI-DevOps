package com.resumeforge.interviewprep;

import com.resumeforge.auth.CurrentUserService;
import com.resumeforge.auth.user.User;
import com.resumeforge.exception.ResourceNotFoundException;
import com.resumeforge.interviewprep.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewPreparationService {

    private final InterviewQuestionBankRepository questionRepository;
    private final UserQuestionProgressRepository progressRepository;
    private final CurrentUserService currentUserService;

    public InterviewPreparationService(
            InterviewQuestionBankRepository questionRepository,
            UserQuestionProgressRepository progressRepository,
            CurrentUserService currentUserService
    ) {
        this.questionRepository = questionRepository;
        this.progressRepository = progressRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<InterviewQuestionDto> getQuestions(
            InterviewQuestionCategory category,
            InterviewQuestionDifficulty difficulty,
            String search,
            Boolean bookmarkedOnly
    ) {
        User user = currentUserService.getCurrentUser();
        List<InterviewQuestionBankItem> questions =
                questionRepository.findAllByOrderByCategoryAscIdAsc();
        Map<Long, UserQuestionProgress> progress = progressRepository
                .findAllByUserId(user.getId())
                .stream()
                .collect(Collectors.toMap(
                        item -> item.getQuestion().getId(),
                        item -> item
                ));
        String normalizedSearch = search == null
                ? ""
                : search.trim().toLowerCase(Locale.ROOT);

        return questions.stream()
                .filter(item -> category == null || item.getCategory() == category)
                .filter(item -> difficulty == null || item.getDifficulty() == difficulty)
                .filter(item -> {
                    if (bookmarkedOnly == null || !bookmarkedOnly) return true;
                    UserQuestionProgress p = progress.get(item.getId());
                    return p != null && p.isFavorite();
                })
                .filter(item -> {
                    if (normalizedSearch.isBlank()) return true;
                    boolean matchTitle = item.getTitle() != null && item.getTitle().toLowerCase(Locale.ROOT).contains(normalizedSearch);
                    boolean matchTopic = item.getTopic() != null && item.getTopic().toLowerCase(Locale.ROOT).contains(normalizedSearch);
                    boolean matchQuestion = item.getQuestion() != null && item.getQuestion().toLowerCase(Locale.ROOT).contains(normalizedSearch);
                    boolean matchAnswer = item.getAnswer() != null && item.getAnswer().toLowerCase(Locale.ROOT).contains(normalizedSearch);
                    boolean matchSource = item.getSourceName() != null && item.getSourceName().toLowerCase(Locale.ROOT).contains(normalizedSearch);
                    return matchTitle || matchTopic || matchQuestion || matchAnswer || matchSource;
                })
                .map(item -> toDto(item, progress.get(item.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public InterviewQuestionDto getQuestion(Long id) {
        User user = currentUserService.getCurrentUser();
        InterviewQuestionBankItem item = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview question not found."
                ));
        UserQuestionProgress progress = progressRepository
                .findByUserIdAndQuestionId(user.getId(), id)
                .orElse(null);
        return toDto(item, progress);
    }

    @Transactional
    public InterviewQuestionDto updateProgress(
            InterviewQuestionProgressRequestDto request
    ) {
        User user = currentUserService.getCurrentUser();
        InterviewQuestionBankItem question = questionRepository
                .findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview question not found."
                ));
        UserQuestionProgress progress = progressRepository
                .findByUserIdAndQuestionId(user.getId(), question.getId())
                .orElseGet(() -> UserQuestionProgress.builder()
                        .user(user)
                        .question(question)
                        .build());
        progress.setCompleted(request.isCompleted());
        progress.setFavorite(request.isFavorite());
        progressRepository.save(progress);
        return toDto(question, progress);
    }

    @Transactional(readOnly = true)
    public InterviewProgressSummaryDto getProgress() {
        User user = currentUserService.getCurrentUser();
        List<InterviewQuestionBankItem> questions =
                questionRepository.findAllByOrderByCategoryAscIdAsc();
        Set<Long> completedQuestionIds = progressRepository
                .findAllByUserId(user.getId())
                .stream()
                .filter(UserQuestionProgress::isCompleted)
                .map(item -> item.getQuestion().getId())
                .collect(Collectors.toSet());
        Map<InterviewQuestionCategory, InterviewCategoryProgressDto> byCategory =
                Arrays.stream(InterviewQuestionCategory.values())
                        .collect(Collectors.toMap(
                                category -> category,
                                category -> InterviewCategoryProgressDto.builder()
                                        .totalQuestions(questions.stream()
                                                .filter(item -> item.getCategory() == category)
                                                .count())
                                        .completedQuestions(questions.stream()
                                                .filter(item -> item.getCategory() == category)
                                                .filter(item -> completedQuestionIds.contains(item.getId()))
                                                .count())
                                        .build()
                        ));
        return InterviewProgressSummaryDto.builder()
                .totalQuestions(questions.size())
                .completedQuestions(
                        progressRepository.countByUserIdAndCompletedTrue(user.getId())
                )
                .favoriteQuestions(
                        progressRepository.countByUserIdAndFavoriteTrue(user.getId())
                )
                .byCategory(byCategory)
                .build();
    }

    private InterviewQuestionDto toDto(
            InterviewQuestionBankItem item,
            UserQuestionProgress progress
    ) {
        String title = item.getTitle();
        if (title == null || title.isBlank()) {
            title = item.getQuestion();
        }
        String topic = item.getTopic();
        if (topic == null || topic.isBlank()) {
            topic = item.getCategory().name().replace('_', ' ');
        }
        String sourceName = item.getSourceName();
        if (sourceName == null || sourceName.isBlank()) {
            sourceName = "Curated Educational Source";
        }
        String sourceUrl = item.getSourceUrl();
        if (sourceUrl == null || sourceUrl.isBlank()) {
            sourceUrl = "https://www.geeksforgeeks.org";
        }

        return InterviewQuestionDto.builder()
                .id(item.getId())
                .category(item.getCategory())
                .difficulty(item.getDifficulty())
                .title(title)
                .topic(topic)
                .question(item.getQuestion())
                .answer(item.getAnswer())
                .sourceName(sourceName)
                .sourceUrl(sourceUrl)
                .completed(progress != null && progress.isCompleted())
                .favorite(progress != null && progress.isFavorite())
                .build();
    }
}
