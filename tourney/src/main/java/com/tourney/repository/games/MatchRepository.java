package com.tourney.repository.games;

import com.tourney.domain.games.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Match> findByPlayer1IdOrPlayer2Id(Long player1Id, Long player2Id);

    @Query("""
        SELECT m FROM Match m
        WHERE m.tournamentRound.tournament.id = :tournamentId
        AND m.tournamentRound.roundNumber = :roundNumber
        AND (m.player1.id = :playerId OR m.player2.id = :playerId)
    """)
    Match findByTournamentAndPlayer(
            @Param("tournamentId") Long tournamentId,
            @Param("playerId") Long playerId,
            @Param("roundNumber") Integer roundNumber
    );

}