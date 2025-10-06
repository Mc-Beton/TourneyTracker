package com.tourney.repository.scores;

import com.tourney.domain.games.MatchRound;
import com.tourney.domain.scores.Score;
import com.tourney.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findAllByMatchRound_Match_TournamentId(Long tournamentId);
    void deleteByMatchRoundAndUser(MatchRound matchRound, User user);

}