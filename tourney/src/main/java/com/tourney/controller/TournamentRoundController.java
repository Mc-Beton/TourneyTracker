package com.tourney.controller;

import com.tourney.domain.games.Match;
import com.tourney.dto.rounds.RoundStartResponseDTO;
import com.tourney.service.games.MatchConversionService;
import com.tourney.service.tournament.TournamentPairingService;
import com.tourney.service.tournament.TournamentRoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/rounds")
@RequiredArgsConstructor
public class TournamentRoundController {
    private final TournamentPairingService tournamentPairingService;
    private final TournamentRoundService tournamentRoundService;
    private final MatchConversionService matchConversionService;

    @PostMapping("/start-next")
    public ResponseEntity<RoundStartResponseDTO> startNextRound(
            @PathVariable Long tournamentId
    ) {
        int nextRoundNumber = tournamentRoundService.getNextRoundNumber(tournamentId);
        tournamentRoundService.validatePreviousRoundCompleted(tournamentId, nextRoundNumber - 1);
        List<Match> matches = tournamentPairingService.createNextRoundPairings(tournamentId, nextRoundNumber);

        RoundStartResponseDTO response = RoundStartResponseDTO.builder()
                .roundNumber(nextRoundNumber)
                .matchCount(matches.size())
                .matches(matches.stream()
                        .map(matchConversionService::toMatchInfo)
                        .toList())
                .build();

        return ResponseEntity.ok(response);
    }
}