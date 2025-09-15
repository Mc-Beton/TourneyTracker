package com.tourney.repository.games;

import com.tourney.domain.games.MatchRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRoundRepository extends JpaRepository<MatchRound, Long> {
    List<MatchRound> findByMatchId(Long matchId);
}