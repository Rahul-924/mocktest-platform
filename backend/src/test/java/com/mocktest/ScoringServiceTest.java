package com.mocktest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mocktest.model.Option;
import com.mocktest.model.Question;
import com.mocktest.service.ScoringService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScoringServiceTest {
    private final ScoringService scoring = new ScoringService();

    private Question q(long id, Option correct) {
        Question q = new Question();
        q.setId(id);
        q.setCorrectOption(correct);
        return q;
    }

    @Test
    void countsOnlyCorrectAnswers() {
        List<Question> qs = List.of(q(1, Option.A), q(2, Option.B), q(3, Option.C));
        Map<Long, Option> answers = Map.of(1L, Option.A, 2L, Option.D, 3L, Option.C);
        assertEquals(2, scoring.score(qs, answers));
    }

    @Test
    void unansweredQuestionsScoreZero() {
        List<Question> qs = List.of(q(1, Option.A), q(2, Option.B));
        assertEquals(0, scoring.score(qs, Map.of()));
    }

    @Test
    void answersForUnknownQuestionsAreIgnored() {
        List<Question> qs = List.of(q(1, Option.A));
        assertEquals(1, scoring.score(qs, Map.of(1L, Option.A, 99L, Option.B)));
    }
}
