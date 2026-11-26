package com.mocktest.repo;

import com.mocktest.model.Attempt;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    Optional<Attempt> findByIdAndUserId(Long id, Long userId);

    Optional<Attempt> findFirstByUserIdAndExamIdAndSubmittedAtIsNullOrderByStartedAtDesc(Long userId, Long examId);

    List<Attempt> findByUserIdAndSubmittedAtIsNotNullOrderByStartedAtDesc(Long userId);

    List<Attempt> findTop50ByExamIdAndSubmittedAtIsNotNullOrderByScoreDescSubmittedAtAsc(Long examId);
}
