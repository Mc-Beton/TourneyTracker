package com.tourney.repository.scores;

import com.tourney.domain.games.MatchRound;
import com.tourney.domain.scores.Score;
import com.tourney.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    void deleteByMatchRoundAndUser(MatchRound matchRound, User user);

    @Query("""
        SELECT s FROM Score s
        WHERE s.matchRound = :matchRound
        AND s.user.id = :playerId
    """)
    Score findByMatchRoundAndPlayerId(
            @Param("matchRound") MatchRound matchRound,
            @Param("playerId") Long playerId
    );

    // To zapytanie zastępuje wadliwą metodę automatyczną
    @Query("SELECT s FROM Score s " +
            "JOIN s.matchRound mr " +
            "JOIN mr.match m " +
            "JOIN m.tournamentRound tr " +
            "JOIN tr.tournament t " +
            "WHERE t.id = :tournamentId")
    List<Score> findAllByTournamentId(@Param("tournamentId") Long tournamentId);
}