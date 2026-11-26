package com.mocktest.service;

import static com.mocktest.dto.Dtos.*;

import com.mocktest.exception.ApiException;
import com.mocktest.model.Attempt;
import com.mocktest.model.Exam;
import com.mocktest.model.Question;
import com.mocktest.repo.AttemptRepository;
import com.mocktest.repo.ExamRepository;
import com.mocktest.repo.QuestionRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository exams;
    private final QuestionRepository questions;
    private final AttemptRepository attempts;

    @Transactional(readOnly = true)
    public List<ExamSummary> list() {
        return exams.findAll().stream().map(this::summary).toList();
    }

    @Transactional
    public ExamSummary create(ExamRequest req) {
        Exam e = new Exam();
        e.setTitle(req.title().trim());
        e.setDescription(req.description());
        e.setDurationMinutes(req.durationMinutes());
        return summary(exams.save(e));
    }

    @Transactional
    public AdminQuestionView addQuestion(Long examId, QuestionRequest req) {
        Exam exam = findExam(examId);
        Question q = new Question();
        q.setExam(exam);
        q.setText(req.text().trim());
        q.setOptionA(req.optionA().trim());
        q.setOptionB(req.optionB().trim());
        q.setOptionC(req.optionC().trim());
        q.setOptionD(req.optionD().trim());
        q.setCorrectOption(req.correctOption());
        return adminView(questions.save(q));
    }

    @Transactional(readOnly = true)
    public AdminExamView adminExam(Long examId) {
        Exam e = findExam(examId);
        List<AdminQuestionView> qs = questions.findByExamIdOrderByIdAsc(examId).stream().map(this::adminView).toList();
        return new AdminExamView(e.getId(), e.getTitle(), e.getDescription(), e.getDurationMinutes(), qs);
    }

    /** Top 10 distinct students by their best score (ties: earliest submission wins). */
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> leaderboard(Long examId) {
        findExam(examId);
        Map<Long, LeaderboardEntry> best = new LinkedHashMap<>();
        for (Attempt a : attempts.findTop50ByExamIdAndSubmittedAtIsNotNullOrderByScoreDescSubmittedAtAsc(examId)) {
            best.putIfAbsent(a.getUser().getId(),
                    new LeaderboardEntry(a.getUser().getName(), a.getScore(), a.getTotal()));
        }
        return best.values().stream().limit(10).toList();
    }

    private Exam findExam(Long id) {
        return exams.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Exam not found"));
    }

    private ExamSummary summary(Exam e) {
        return new ExamSummary(e.getId(), e.getTitle(), e.getDescription(), e.getDurationMinutes(),
                questions.countByExamId(e.getId()));
    }

    private AdminQuestionView adminView(Question q) {
        return new AdminQuestionView(q.getId(), q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(),
                q.getOptionD(), q.getCorrectOption().name());
    }
}
