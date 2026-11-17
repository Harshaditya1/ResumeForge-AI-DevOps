package com.resumeforge.interviewprep;

import com.resumeforge.auth.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_question_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_question_progress",
                columnNames = {"user_id", "question_id"}
        ),
        indexes = @Index(
                name = "idx_progress_user_completed",
                columnList = "user_id,completed"
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuestionProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestionBankItem question;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private boolean favorite;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
