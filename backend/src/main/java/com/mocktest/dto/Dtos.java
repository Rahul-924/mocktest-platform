package com.mocktest.dto;

import com.mocktest.model.Option;
import com.mocktest.model.Question;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** All request/response shapes in one place. Entities are never returned directly. */
public final class Dtos {
    private Dtos() { }

    // ----- auth
    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72) String password) { }   // 72 = BCrypt's byte limit

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) { }

    public record AuthResponse(String token, String name, String role) { }

    // ----- exams (student view)
    public record ExamSummary(Long id, String title, String description, int durationMinutes, long questionCount) { }

    /** A question as shown DURING an exam: note there is no correct answer field. */
    public record QuestionView(Long id, String text, String optionA, String optionB, String optionC, String optionD) {
        public static QuestionView from(Question q) {
            return new QuestionView(q.getId(), q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD());
        }
    }

    public record StartResponse(Long attemptId, String examTitle, Instant deadline, List<QuestionView> questions) { }

    public record SubmitRequest(Map<Long, String> answers) { }   // questionId -> "A".."D"

    public record QuestionReview(Long id, String text, String optionA, String optionB, String optionC,
                                 String optionD, String selected, String correct) { }

    public record ResultView(Long attemptId, String examTitle, int score, int total, Instant submittedAt,
                             List<QuestionReview> review) { }

    public record AttemptSummary(Long id, Long examId, String examTitle, int score, int total, Instant submittedAt) { }

    public record LeaderboardEntry(String name, int score, int total) { }

    // ----- admin
    public record ExamRequest(
            @NotBlank @Size(max = 150) String title,
            @Size(max = 500) String description,
            @Min(1) @Max(300) int durationMinutes) { }

    public record QuestionRequest(
            @NotBlank @Size(max = 1000) String text,
            @NotBlank @Size(max = 300) String optionA,
            @NotBlank @Size(max = 300) String optionB,
            @NotBlank @Size(max = 300) String optionC,
            @NotBlank @Size(max = 300) String optionD,
            @NotNull Option correctOption) { }

    public record AdminQuestionView(Long id, String text, String optionA, String optionB, String optionC,
                                    String optionD, String correctOption) { }

    public record AdminExamView(Long id, String title, String description, int durationMinutes,
                                List<AdminQuestionView> questions) { }
}
