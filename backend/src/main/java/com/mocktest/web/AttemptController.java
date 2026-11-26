package com.mocktest.web;

import static com.mocktest.dto.Dtos.*;

import com.mocktest.security.AuthUser;
import com.mocktest.service.AttemptService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {
    private final AttemptService attempts;

    @PostMapping("/{id}/submit")
    public ResultView submit(@PathVariable Long id, @RequestBody SubmitRequest req, @AuthenticationPrincipal AuthUser user) {
        return attempts.submit(user.id(), id, req.answers());
    }

    @GetMapping("/my")
    public List<AttemptSummary> mine(@AuthenticationPrincipal AuthUser user) { return attempts.myAttempts(user.id()); }

    @GetMapping("/{id}")
    public ResultView result(@PathVariable Long id, @AuthenticationPrincipal AuthUser user) {
        return attempts.result(user.id(), id);
    }
}
