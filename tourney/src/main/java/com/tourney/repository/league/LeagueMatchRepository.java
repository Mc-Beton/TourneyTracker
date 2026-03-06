package com.tourney.repository.league;

import com.tourney.domain.league.League;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.league.LeagueMatch;
import com.tourney.domain.games.SingleMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeagueMatchRepository extends JpaRepository<LeagueMatch, Long> {
    Optional<LeagueMatch> findByLeagueAndMatch(League league, SingleMatch match);
    Optional<LeagueMatch> findByMatch(SingleMatch match);
    Page<LeagueMatch> findByLeagueAndMatchStatus(League league, MatchStatus status, Pageable pageable);
    Page<LeagueMatch> findByMatchStatus(MatchStatus status, Pageable pageable);
    Page<LeagueMatch> findByLeague(League league, Pageable pageable);
}
