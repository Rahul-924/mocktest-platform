package com.mocktest.web;

import static com.mocktest.dto.Dtos.*;

import com.mocktest.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** Every route under /api/admin/** is restricted to ROLE_ADMIN in SecurityConfig. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ExamService exams;

    @PostMapping("/exams")
    public ExamSummary createExam(@Valid @RequestBody ExamRequest req) { return exams.create(req); }

    @GetMapping("/exams/{id}")
    public AdminExamView getExam(@PathVariable Long id) { return exams.adminExam(id); }

    @PostMapping("/exams/{id}/questions")
    public AdminQuestionView addQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest req) {
        return exams.addQuestion(id, req);
    }
}
