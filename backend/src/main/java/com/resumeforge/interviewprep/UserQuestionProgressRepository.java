package com.resumeforge.interviewprep;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserQuestionProgressRepository
        extends JpaRepository<UserQuestionProgress, Long> {

    Optional<UserQuestionProgress> findByUserIdAndQuestionId(
            Long userId,
            Long questionId
    );

    List<UserQuestionProgress> findAllByUserId(Long userId);

    long countByUserIdAndCompletedTrue(Long userId);

    long countByUserIdAndFavoriteTrue(Long userId);
}
