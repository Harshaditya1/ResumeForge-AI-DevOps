package com.resumeforge.interviewprep;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "interview_question_bank",
        indexes = {
                @Index(name = "idx_question_category", columnList = "category"),
                @Index(name = "idx_question_difficulty", columnList = "difficulty")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionBankItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InterviewQuestionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InterviewQuestionDifficulty difficulty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;
}
