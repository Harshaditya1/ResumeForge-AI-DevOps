package com.resumeforge.interviewprep;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewQuestionBankRepository
        extends JpaRepository<InterviewQuestionBankItem, Long> {

    List<InterviewQuestionBankItem> findAllByOrderByCategoryAscIdAsc();

    List<InterviewQuestionBankItem> findAllByCategoryOrderByIdAsc(
            InterviewQuestionCategory category
    );

    List<InterviewQuestionBankItem> findAllByDifficultyOrderByIdAsc(
            InterviewQuestionDifficulty difficulty
    );

    long countByCategory(InterviewQuestionCategory category);
}
