package com.tourney.repository.scores;

import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByUserId(Long userId);
    List<Score> findByMatchRoundId(Long matchRoundId);
    List<Score> findByScoreType(ScoreType scoreType);
}