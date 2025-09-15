package com.tourney.repository.games;

import com.tourney.domain.games.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Match> findByPlayer1IdOrPlayer2Id(Long player1Id, Long player2Id);
}