package com.tourney.repository.games;
import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.games.SingleMatch;
import com.tourney.domain.games.TournamentMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT m FROM Match m WHERE m.player1.id = :userId OR m.player2.id = :userId")
    List<Match> findByPlayer1IdOrPlayer2Id(@Param("userId") Long player1Id, @Param("userId") Long player2Id);

    // NOTE: previously this method returned a single result and could throw NonUniqueResultException.
    // We now return a list sorted by id DESC and pick deterministically in service layer.
    @Query("""
        SELECT m FROM TournamentMatch m
        WHERE m.tournamentRound.tournament.id = :tournamentId
          AND m.tournamentRound.roundNumber = :roundNumber
          AND (m.player1.id = :playerId OR m.player2.id = :playerId)
        ORDER BY m.id DESC
    """)
    List<TournamentMatch> findAllByTournamentAndPlayerInRound(
            @Param("tournamentId") Long tournamentId,
            @Param("playerId") Long playerId,
            @Param("roundNumber") Integer roundNumber
    );

    // Fallback: any IN_PROGRESS match for player in the tournament, latest round first
    @Query("""
        SELECT m FROM TournamentMatch m
        WHERE m.tournamentRound.tournament.id = :tournamentId
          AND (m.player1.id = :playerId OR m.player2.id = :playerId)
          AND m.status = com.tourney.domain.games.MatchStatus.IN_PROGRESS
        ORDER BY m.tournamentRound.roundNumber DESC, m.id DESC
    """)
    List<TournamentMatch> findInProgressForPlayerInTournament(
            @Param("tournamentId") Long tournamentId,
            @Param("playerId") Long playerId
    );

    @Query("""
        SELECT m FROM SingleMatch m
        LEFT JOIN FETCH m.details d
        LEFT JOIN FETCH d.gameSystem gs
        LEFT JOIN FETCH m.player1 p1
        LEFT JOIN FETCH m.player2 p2
        WHERE (p1.id = :userId OR p2.id = :userId)
        ORDER BY m.startTime DESC
    """)
    List<SingleMatch> findMySingleMatches(@Param("userId") Long userId);

    @Query("""
        SELECT m FROM Match m
        LEFT JOIN FETCH m.player1 p1
        LEFT JOIN FETCH m.player2 p2
        LEFT JOIN FETCH m.details d
        LEFT JOIN FETCH d.gameSystem gs
        LEFT JOIN FETCH d.primaryMission pm
        LEFT JOIN FETCH d.deployment dep
        LEFT JOIN FETCH m.rounds r
        WHERE m.id = :matchId
    """)
    Optional<Match> findMatchDetailsView(@Param("matchId") Long matchId);

    @Query("""
        SELECT m FROM Match m
        LEFT JOIN FETCH m.player1 p1
        LEFT JOIN FETCH m.player2 p2
        LEFT JOIN FETCH m.details d
        LEFT JOIN FETCH d.gameSystem gs
        LEFT JOIN FETCH d.primaryMission pm
        LEFT JOIN FETCH d.deployment dep
        LEFT JOIN FETCH m.rounds r
        WHERE m.id = :matchId
    """)
    Optional<Match> findMatchSummaryView(@Param("matchId") Long matchId);
}