package com.tourney.controller.tournament;

import com.tourney.domain.games.Match;
import com.tourney.dto.rounds.RoundCompletionSummaryDTO;
import com.tourney.dto.rounds.RoundStartResponseDTO;
import com.tourney.dto.tournament.RoundStatusDTO;
import com.tourney.dto.tournament.TournamentRoundViewDTO;
import com.tourney.service.games.MatchConversionService;
import com.tourney.service.tournament.SubsequentRoundPairingService;
import com.tourney.service.tournament.TournamentRoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tourney.service.tournament.FirstRoundPairingService;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/rounds")
@RequiredArgsConstructor
public class TournamentRoundController {
    private final FirstRoundPairingService firstRoundPairingService;
    private final SubsequentRoundPairingService subsequentRoundPairingService;
    private final TournamentRoundService tournamentRoundService;
    private final MatchConversionService matchConversionService;

    @PostMapping("/start-first")
    public ResponseEntity<RoundStartResponseDTO> startFirstRound(
            @PathVariable Long tournamentId
    ) {
        try {
            List<Match> matches = firstRoundPairingService.createFirstRoundPairings(tournamentId);
            
            RoundStartResponseDTO response = RoundStartResponseDTO.builder()
                    .roundNumber(1)
                    .matchCount(matches.size())
                    .matches(matches.stream()
                            .map(matchConversionService::toMatchInfo)
                            .toList())
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Błąd podczas tworzenia par pierwszej rundy: " + e.getMessage(), e);
        }
    }

    @PostMapping("/start-next")
    public ResponseEntity<RoundStartResponseDTO> startNextRound(
            @PathVariable Long tournamentId
    ) {
        int nextRoundNumber = tournamentRoundService.getNextRoundNumber(tournamentId);
        tournamentRoundService.validatePreviousRoundCompleted(tournamentId, nextRoundNumber - 1);
        
        List<Match> matches = subsequentRoundPairingService.createNextRoundPairings(
                tournamentId,
                nextRoundNumber
        );

        RoundStartResponseDTO response = RoundStartResponseDTO.builder()
                .roundNumber(nextRoundNumber)
                .matchCount(matches.size())
                .matches(matches.stream()
                        .map(matchConversionService::toMatchInfo)
                        .toList())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roundNumber}/complete")
    public ResponseEntity<RoundCompletionSummaryDTO> completeRound(
            @PathVariable Long tournamentId,
            @PathVariable int roundNumber
    ) {
        RoundCompletionSummaryDTO summary = tournamentRoundService.completeRound(tournamentId, roundNumber);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{roundNumber}/status")
    public ResponseEntity<RoundCompletionSummaryDTO> getRoundStatus(
            @PathVariable Long tournamentId,
            @PathVariable int roundNumber
    ) {
        RoundCompletionSummaryDTO summary = tournamentRoundService.getRoundStatus(tournamentId, roundNumber);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/{roundNumber}/start")
    public ResponseEntity<Void> startRound(
            @PathVariable Long tournamentId,
            @PathVariable int roundNumber
    ) {
        tournamentRoundService.startRound(tournamentId, roundNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{roundNumber}/extend")
    public ResponseEntity<Void> extendSubmissionDeadline(
            @PathVariable Long tournamentId,
            @PathVariable int roundNumber,
            @RequestParam(defaultValue = "5") int additionalMinutes
    ) {
        tournamentRoundService.extendSubmissionDeadline(tournamentId, roundNumber, additionalMinutes);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roundNumber}/organizer-status")
    public ResponseEntity<RoundStatusDTO> getOrganizerRoundStatus(
            @PathVariable Long tournamentId,
            @PathVariable int roundNumber
    ) {
        RoundStatusDTO status = tournamentRoundService.getRoundStatusForOrganizer(tournamentId, roundNumber);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TournamentRoundViewDTO>> getAllRoundsView(
            @PathVariable Long tournamentId
    ) {
        List<TournamentRoundViewDTO> rounds = tournamentRoundService.getTournamentRoundsView(tournamentId);
        return ResponseEntity.ok(rounds);
    }

    @PostMapping("/{roundNumber}/matches/{matchId}/start")
    public ResponseEntity<Void> startIndividualMatch(
            @PathVariable Long tournamentId,
            @PathVariable int roundNumber,
            @PathVariable Long matchId
    ) {
        tournamentRoundService.startIndividualMatch(tournamentId, roundNumber, matchId);
        return ResponseEntity.ok().build();
    }

}