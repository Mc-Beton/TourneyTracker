package com.tourney.controller.match;

import com.common.security.UserPrincipal;
import com.tourney.dto.matches.MatchScoringDTO;
import com.tourney.dto.scores.SubmitScoreDTO;
import com.tourney.service.match.MatchScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchScoringController {

    private final MatchScoringService matchScoringService;

    /**
     * Pobierz dane meczu do wpisywania wyników
     */
    @GetMapping("/{matchId}/scoring")
    public ResponseEntity<MatchScoringDTO> getMatchScoring(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(matchScoringService.getMatchScoring(matchId, currentUser.getId())
        );
    }

    /**
     * Zapisz wyniki rundy (może być partial - tylko niektóre wyniki)
     */
    @PostMapping("/{matchId}/scores")
    public ResponseEntity<MatchScoringDTO> submitRoundScores(
            @PathVariable Long matchId,
            @RequestBody SubmitScoreDTO submitScoreDTO,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(matchScoringService.submitRoundScores(
                        matchId,
                        submitScoreDTO,
                        currentUser.getId()
                )
        );
    }

    /**
     * Rozpocznij nową rundę
     */
    @PostMapping("/{matchId}/rounds/start")
    public ResponseEntity<MatchScoringDTO> startRound(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(matchScoringService.startRound(matchId, currentUser.getId())
        );
    }

    /**
     * Zakończ rundę
     */
    @PostMapping("/{matchId}/rounds/{roundNumber}/end")
    public ResponseEntity<MatchScoringDTO> endRound(
            @PathVariable Long matchId,
            @PathVariable int roundNumber,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(matchScoringService.endRound(matchId, roundNumber, currentUser.getId())
        );
    }
}