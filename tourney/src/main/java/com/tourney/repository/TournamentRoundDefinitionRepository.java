package com.tourney.repository;

import com.tourney.domain.tournament.TournamentRoundDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentRoundDefinitionRepository extends JpaRepository<TournamentRoundDefinition, Long> {
    List<TournamentRoundDefinition> findByTournamentIdOrderByRoundNumberAsc(Long tournamentId);
    Optional<TournamentRoundDefinition> findByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);
}
