package com.mocktest.web;

import static com.mocktest.dto.Dtos.*;

import com.mocktest.security.AuthUser;
import com.mocktest.service.AttemptService;
import com.mocktest.service.ExamService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService exams;
    private final AttemptService attempts;

    @GetMapping
    public List<ExamSummary> list() { return exams.list(); }

    @PostMapping("/{id}/start")
    public StartResponse start(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return attempts.start(user.id(), id);
    }

    @GetMapping("/{id}/leaderboard")
    public List<LeaderboardEntry> leaderboard(@PathVariable Long id) { return exams.leaderboard(id); }
}
