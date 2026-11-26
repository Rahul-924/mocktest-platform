package com.mocktest.service;

import static com.mocktest.dto.Dtos.*;

import com.mocktest.exception.ApiException;
import com.mocktest.model.Attempt;
import com.mocktest.model.AttemptAnswer;
import com.mocktest.model.Exam;
import com.mocktest.model.Option;
import com.mocktest.model.Question;
import com.mocktest.repo.AttemptAnswerRepository;
import com.mocktest.repo.AttemptRepository;
import com.mocktest.repo.ExamRepository;
import com.mocktest.repo.QuestionRepository;
import com.mocktest.repo.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The core of the app: starting a timed attempt, grading it on the server, and showing results. */
@Service
@RequiredArgsConstructor
public class AttemptService {
    private static final long GRACE_SECONDS = 30;   // allowance for network delay on auto-submit

    private final AttemptRepository attempts;
    private final AttemptAnswerRepository answerRepo;
    private final ExamRepository exams;
    private final QuestionRepository questions;
    private final UserRepository users;
    private final ScoringService scoring;

    /** Starts an attempt, or resumes the one already running (so a page refresh does not reset the timer). */
    @Transactional
    public StartResponse start(Long userId, Long examId) {
        Exam exam = exams.findById(examId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Exam not found"));
        List<Question> qs = questions.findByExamIdOrderByIdAsc(examId);
        if (qs.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "This exam has no questions yet");

        Instant now = Instant.now();
        Attempt attempt = attempts
                .findFirstByUserIdAndExamIdAndSubmittedAtIsNullOrderByStartedAtDesc(userId, examId).orElse(null);

        if (attempt != null && now.isAfter(deadline(attempt))) {   // old attempt ran out of time: close it with 0
            close(attempt, 0, qs.size(), now);
            attempt = null;
        }
        if (attempt == null) {
            attempt = new Attempt();
            attempt.setUser(users.getReferenceById(userId));
            attempt.setExam(exam);
            attempt.setStartedAt(now);
            attempt = attempts.save(attempt);
        }
        return new StartResponse(attempt.getId(), exam.getTitle(), deadline(attempt),
                qs.stream().map(QuestionView::from).toList());
    }

    @Transactional
    public ResultView submit(Long userId, Long attemptId, Map<Long, String> rawAnswers) {
        Attempt attempt = findOwned(userId, attemptId);
        if (attempt.getSubmittedAt() != null) throw new ApiException(HttpStatus.BAD_REQUEST, "Already submitted");

        List<Question> qs = questions.findByExamIdOrderByIdAsc(attempt.getExam().getId());
        Instant now = Instant.now();
        boolean tooLate = now.isAfter(deadline(attempt).plusSeconds(GRACE_SECONDS));

        // The server decides what counts: late submissions get no credit, and only real question ids are used.
        Map<Long, Option> answers = new HashMap<>();
        if (!tooLate && rawAnswers != null) {
            for (Question q : qs) {
                String v = rawAnswers.get(q.getId());
                if (v != null) answers.put(q.getId(), parseOption(v));
            }
        }
        List<AttemptAnswer> rows = new ArrayList<>();
        for (Question q : qs) {
            Option chosen = answers.get(q.getId());
            if (chosen == null) continue;
            AttemptAnswer row = new AttemptAnswer();
            row.setAttempt(attempt);
            row.setQuestion(q);
            row.setSelected(chosen);
            rows.add(row);
        }
        answerRepo.saveAll(rows);
        close(attempt, scoring.score(qs, answers), qs.size(), now);
        return buildResult(attempt, qs, answers);
    }

    @Transactional(readOnly = true)
    public ResultView result(Long userId, Long attemptId) {
        Attempt attempt = findOwned(userId, attemptId);
        if (attempt.getSubmittedAt() == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Attempt is still in progress");
        List<Question> qs = questions.findByExamIdOrderByIdAsc(attempt.getExam().getId());
        Map<Long, Option> answers = new HashMap<>();
        for (AttemptAnswer a : answerRepo.findByAttemptId(attemptId)) {
            answers.put(a.getQuestion().getId(), a.getSelected());
        }
        return buildResult(attempt, qs, answers);
    }

    @Transactional(readOnly = true)
    public List<AttemptSummary> myAttempts(Long userId) {
        return attempts.findByUserIdAndSubmittedAtIsNotNullOrderByStartedAtDesc(userId).stream()
                .map(a -> new AttemptSummary(a.getId(), a.getExam().getId(), a.getExam().getTitle(),
                        a.getScore(), a.getTotal(), a.getSubmittedAt()))
                .toList();
    }

    // ---------- helpers
    private Attempt findOwned(Long userId, Long attemptId) {
        // Looking up by id AND user id means students can never read someone else's attempt (no IDOR).
        return attempts.findByIdAndUserId(attemptId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Attempt not found"));
    }

    private Instant deadline(Attempt a) {
        return a.getStartedAt().plus(a.getExam().getDurationMinutes(), ChronoUnit.MINUTES);
    }

    private void close(Attempt a, int score, int total, Instant now) {
        a.setScore(score);
        a.setTotal(total);
        a.setSubmittedAt(now);
        attempts.save(a);
    }

    private Option parseOption(String value) {
        try {
            return Option.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid answer option");
        }
    }

    private ResultView buildResult(Attempt a, List<Question> qs, Map<Long, Option> answers) {
        List<QuestionReview> review = qs.stream().map(q -> {
            Option sel = answers.get(q.getId());
            return new QuestionReview(q.getId(), q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(),
                    q.getOptionD(), sel == null ? null : sel.name(), q.getCorrectOption().name());
        }).toList();
        return new ResultView(a.getId(), a.getExam().getTitle(), a.getScore(), a.getTotal(), a.getSubmittedAt(), review);
    }
}
