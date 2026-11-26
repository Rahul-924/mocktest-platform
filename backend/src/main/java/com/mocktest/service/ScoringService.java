package com.mocktest.service;

import com.mocktest.model.Option;
import com.mocktest.model.Question;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Pure scoring logic with no database access, so it is trivial to unit test. */
@Service
public class ScoringService {
    /** One point per question whose selected option equals the correct option. Unanswered = 0. */
    public int score(List<Question> questions, Map<Long, Option> answers) {
        int score = 0;
        for (Question q : questions) {
            if (q.getCorrectOption() == answers.get(q.getId())) score++;
        }
        return score;
    }
}
