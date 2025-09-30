package com.tourney.service.tournament;

import com.tourney.domain.tournament.Tournament;
import com.tourney.exception.RoundNotCompletedException;
import com.tourney.exception.TournamentNotFoundException;
import com.tourney.repository.tournament.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentRoundService {
    private final TournamentRepository tournamentRepository;

    public int getNextRoundNumber(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        return tournament.getRounds().stream()
                .mapToInt(round -> round.getRoundNumber())
                .max()
                .orElse(0) + 1;
    }

    public void validatePreviousRoundCompleted(Long tournamentId, int previousRoundNumber) {
        if (previousRoundNumber < 1) {
            return; // Pierwsza runda nie wymaga walidacji
        }

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        boolean isCompleted = tournament.getRounds().stream()
                .filter(round -> round.getRoundNumber() == previousRoundNumber)
                .findFirst()
                .map(round -> round.getMatches().stream()
                        .allMatch(match -> match.getMatchResult() != null))
                .orElse(false);

        if (!isCompleted) {
            throw new RoundNotCompletedException(tournamentId, previousRoundNumber);
        }
    }
}