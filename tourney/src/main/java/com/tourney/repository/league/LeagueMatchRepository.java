package com.tourney.repository.league;

import com.tourney.domain.league.League;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.league.LeagueMatch;
import com.tourney.domain.games.SingleMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeagueMatchRepository extends JpaRepository<LeagueMatch, Long> {
    Optional<LeagueMatch> findByLeagueAndMatch(League league, SingleMatch match);
    Optional<LeagueMatch> findByMatch(SingleMatch match);
    Page<LeagueMatch> findByLeagueAndMatchStatus(League league, MatchStatus status, Pageable pageable);
    Page<LeagueMatch> findByMatchStatus(MatchStatus status, Pageable pageable);
    
    @Query(value = "SELECT DISTINCT lm FROM LeagueMatch lm " +
           "LEFT JOIN FETCH lm.match m " +
           "LEFT JOIN FETCH m.matchResult mr " +
           "LEFT JOIN FETCH m.player1 " +
           "LEFT JOIN FETCH m.player2 " +
           "LEFT JOIN FETCH m.details " +
           "LEFT JOIN FETCH lm.submittedBy " +
           "WHERE lm.league = :league",
           countQuery = "SELECT COUNT(lm) FROM LeagueMatch lm WHERE lm.league = :league")
    Page<LeagueMatch> findByLeagueWithMatchData(@Param("league") League league, Pageable pageable);
    
    @Query(value = "SELECT DISTINCT lm FROM LeagueMatch lm " +
           "LEFT JOIN FETCH lm.match m " +
           "LEFT JOIN FETCH m.matchResult mr " +
           "LEFT JOIN FETCH m.player1 " +
           "LEFT JOIN FETCH m.player2 " +
           "LEFT JOIN FETCH m.details " +
           "LEFT JOIN FETCH lm.submittedBy " +
           "WHERE lm.league = :league AND m.status = :status",
           countQuery = "SELECT COUNT(lm) FROM LeagueMatch lm LEFT JOIN lm.match m WHERE lm.league = :league AND m.status = :status")
    Page<LeagueMatch> findByLeagueAndMatchStatusWithData(@Param("league") League league, @Param("status") MatchStatus status, Pageable pageable);
    
    Page<LeagueMatch> findByLeague(League league, Pageable pageable);
}
