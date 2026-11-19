package com.resumeforge.application;

import com.resumeforge.auth.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_applications",
        indexes = {
                @Index(name = "idx_job_app_user_status", columnList = "user_id,status"),
                @Index(name = "idx_job_app_user_created", columnList = "user_id,created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 160)
    private String companyName;

    @Column(nullable = false, length = 160)
    private String jobTitle;

    @Column(length = 160)
    private String location;

    @Column(length = 60)
    private String experienceLevel;

    @Column(length = 60)
    private String jobType;

    @Column(length = 100)
    private String source;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Column(length = 1000)
    private String applicationUrl;

    private LocalDate appliedDate;

    private LocalDate interviewDate;

    private LocalDate deadline;

    @Column(length = 100)
    private String externalJobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobApplicationStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = JobApplicationStatus.SAVED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
