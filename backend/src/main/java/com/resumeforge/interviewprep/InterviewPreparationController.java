package com.resumeforge.interviewprep;

import com.resumeforge.interviewprep.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interview-prep")
@CrossOrigin(origins = "http://localhost:5173")
public class InterviewPreparationController {

    private final InterviewPreparationService service;

    public InterviewPreparationController(InterviewPreparationService service) {
        this.service = service;
    }

    @GetMapping("/questions")
    public List<InterviewQuestionDto> getQuestions(
            @RequestParam(required = false) InterviewQuestionCategory category,
            @RequestParam(required = false) InterviewQuestionDifficulty difficulty,
            @RequestParam(required = false) String search
    ) {
        return service.getQuestions(category, difficulty, search);
    }

    @GetMapping("/questions/{id}")
    public InterviewQuestionDto getQuestion(@PathVariable Long id) {
        return service.getQuestion(id);
    }

    @GetMapping("/progress")
    public InterviewProgressSummaryDto getProgress() {
        return service.getProgress();
    }

    @PostMapping("/progress")
    public InterviewQuestionDto updateProgress(
            @Valid @RequestBody InterviewQuestionProgressRequestDto request
    ) {
        return service.updateProgress(request);
    }
}
