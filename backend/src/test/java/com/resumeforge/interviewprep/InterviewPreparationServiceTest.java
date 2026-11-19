package com.resumeforge.interviewprep;

import com.resumeforge.auth.CurrentUserService;
import com.resumeforge.auth.user.User;
import com.resumeforge.interviewprep.dto.InterviewProgressSummaryDto;
import com.resumeforge.interviewprep.dto.InterviewQuestionDto;
import com.resumeforge.interviewprep.dto.InterviewQuestionProgressRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewPreparationServiceTest {

    @Mock
    private InterviewQuestionBankRepository questionRepository;

    @Mock
    private UserQuestionProgressRepository progressRepository;

    @Mock
    private CurrentUserService currentUserService;

    private InterviewPreparationService service;

    private User testUser;
    private InterviewQuestionBankItem javaQuestion;
    private InterviewQuestionBankItem dsaQuestion;

    @BeforeEach
    void setUp() {
        service = new InterviewPreparationService(questionRepository, progressRepository, currentUserService);

        testUser = User.builder().id(1L).email("test@example.com").build();

        javaQuestion = InterviewQuestionBankItem.builder()
                .id(101L)
                .category(InterviewQuestionCategory.JAVA)
                .difficulty(InterviewQuestionDifficulty.EASY)
                .title("Why is String immutable in Java?")
                .topic("Core Java")
                .question("Explain why String is immutable in Java.")
                .answer("Thread safety, security, and string interning.")
                .sourceName("GeeksforGeeks")
                .sourceUrl("https://www.geeksforgeeks.org/string-immutability-in-java/")
                .build();

        dsaQuestion = InterviewQuestionBankItem.builder()
                .id(102L)
                .category(InterviewQuestionCategory.DSA)
                .difficulty(InterviewQuestionDifficulty.MEDIUM)
                .title("Two Sum Problem")
                .topic("Arrays")
                .question("Find two numbers that sum to target.")
                .answer("Use a hash map for O(n) lookup.")
                .sourceName("LeetCode")
                .sourceUrl("https://leetcode.com/problems/two-sum/")
                .build();
    }

    @Test
    void testGetQuestionsWithCategoryFilter() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(questionRepository.findAllByOrderByCategoryAscIdAsc()).thenReturn(List.of(javaQuestion, dsaQuestion));
        when(progressRepository.findAllByUserId(1L)).thenReturn(List.of());

        List<InterviewQuestionDto> result = service.getQuestions(InterviewQuestionCategory.JAVA, null, null, false);

        assertEquals(1, result.size());
        assertEquals("Why is String immutable in Java?", result.get(0).getTitle());
        assertEquals("GeeksforGeeks", result.get(0).getSourceName());
        assertEquals("https://www.geeksforgeeks.org/string-immutability-in-java/", result.get(0).getSourceUrl());
        assertFalse(result.get(0).isCompleted());
    }

    @Test
    void testUpdateProgressAndFavorite() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(questionRepository.findById(101L)).thenReturn(Optional.of(javaQuestion));
        when(progressRepository.findByUserIdAndQuestionId(1L, 101L)).thenReturn(Optional.empty());

        InterviewQuestionProgressRequestDto request = new InterviewQuestionProgressRequestDto();
        request.setQuestionId(101L);
        request.setCompleted(true);
        request.setFavorite(true);

        InterviewQuestionDto updated = service.updateProgress(request);

        assertTrue(updated.isCompleted());
        assertTrue(updated.isFavorite());
        verify(progressRepository, times(1)).save(any(UserQuestionProgress.class));
    }

    @Test
    void testGetProgressSummary() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(questionRepository.findAllByOrderByCategoryAscIdAsc()).thenReturn(List.of(javaQuestion, dsaQuestion));
        when(progressRepository.findAllByUserId(1L)).thenReturn(List.of(
                UserQuestionProgress.builder().user(testUser).question(javaQuestion).completed(true).favorite(true).build()
        ));
        when(progressRepository.countByUserIdAndCompletedTrue(1L)).thenReturn(1L);
        when(progressRepository.countByUserIdAndFavoriteTrue(1L)).thenReturn(1L);

        InterviewProgressSummaryDto summary = service.getProgress();

        assertEquals(2, summary.getTotalQuestions());
        assertEquals(1, summary.getCompletedQuestions());
        assertEquals(1, summary.getFavoriteQuestions());
        assertNotNull(summary.getByCategory().get(InterviewQuestionCategory.JAVA));
        assertEquals(1, summary.getByCategory().get(InterviewQuestionCategory.JAVA).getCompletedQuestions());
    }
}

