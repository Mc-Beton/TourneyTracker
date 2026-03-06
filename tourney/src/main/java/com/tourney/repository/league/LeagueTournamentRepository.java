package com.tourney.repository.league;

import com.tourney.domain.league.League;
import com.tourney.dto.tournament.TournamentStatus;
import com.tourney.domain.league.LeagueTournament;
import com.tourney.domain.tournament.Tournament;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeagueTournamentRepository extends JpaRepository<LeagueTournament, Long> {
    
    Optional<LeagueTournament> findByLeagueAndTournament(League league, Tournament tournament);
    
    Page<LeagueTournament> findByLeagueAndTournamentStatus(League league, TournamentStatus status, Pageable pageable);
    

}
