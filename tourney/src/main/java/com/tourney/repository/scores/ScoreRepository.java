package com.tourney.repository.scores;

import com.tourney.domain.scores.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findAllByMatchRound_Match_TournamentId(Long tournamentId);
}