package com.tourney.repository.league;

import com.tourney.domain.league.League;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.league.LeagueMatch;
import com.tourney.domain.games.SingleMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
    long countByLeague(League league);
    
    @EntityGraph(attributePaths = {"match", "match.matchResult", "match.player1", "match.player2", "match.details", "submittedBy"})
    Page<LeagueMatch> findByLeague(League league, Pageable pageable);
    
    @EntityGraph(attributePaths = {"match", "match.matchResult", "match.player1", "match.player2", "match.details", "submittedBy"})
    @Query("SELECT lm FROM LeagueMatch lm WHERE lm.league = :league AND lm.match.status = :status")
    Page<LeagueMatch> findByLeagueAndMatchStatusWithData(@Param("league") League league, @Param("status") MatchStatus status, Pageable pageable);
}
