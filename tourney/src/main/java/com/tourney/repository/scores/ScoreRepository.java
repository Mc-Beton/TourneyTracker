package com.tourney.repository.scores;

import com.tourney.domain.games.MatchRound;
import com.tourney.domain.scores.MatchSide;
import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import com.common.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Query("""
        SELECT s FROM Score s
        JOIN FETCH s.matchRound r
        JOIN FETCH s.user u
        WHERE r.match.id = :matchId
    """)
    List<Score> findAllByMatchIdWithRoundAndUser(@Param("matchId") Long matchId);

    @Modifying
    @Query("DELETE FROM Score s WHERE s.matchRound.id = :roundId AND s.side = :side")
    void deleteByMatchRoundIdAndSide(@Param("roundId") Long roundId, @Param("side") MatchSide side);

    @Query("""
        SELECT s FROM Score s
        WHERE s.matchRound.id = :roundId
          AND s.side = :side
    """)
    List<Score> findByMatchRoundIdAndSide(@Param("roundId") Long roundId, @Param("side") MatchSide side);

    @Query("""
        SELECT s FROM Score s
        JOIN FETCH s.matchRound r
        WHERE r.match.id = :matchId
    """)
    List<Score> findAllByMatchIdWithRound(@Param("matchId") Long matchId);

    Optional<Score> findByMatchRoundIdAndSideAndScoreType(Long matchRoundId, MatchSide side, ScoreType scoreType);

    List<Score> findByMatchRoundId(Long matchRoundId);

}