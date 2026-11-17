package com.resumeforge.resumehealth;

import com.resumeforge.auth.user.User;
import com.resumeforge.resume.Resume;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "resume_health_checklist",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resume_health_checklist",
                columnNames = {"user_id", "resume_id", "section_key"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeHealthChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "section_key", nullable = false, length = 80)
    private String sectionKey;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
