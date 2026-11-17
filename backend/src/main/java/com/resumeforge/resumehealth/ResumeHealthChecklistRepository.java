package com.resumeforge.resumehealth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeHealthChecklistRepository
        extends JpaRepository<ResumeHealthChecklist, Long> {

    List<ResumeHealthChecklist> findAllByUserIdAndResumeId(
            Long userId,
            Long resumeId
    );

    Optional<ResumeHealthChecklist> findByUserIdAndResumeIdAndSectionKey(
            Long userId,
            Long resumeId,
            String sectionKey
    );
}
