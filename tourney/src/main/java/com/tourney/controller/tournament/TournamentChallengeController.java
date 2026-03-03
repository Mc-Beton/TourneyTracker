package com.tourney.controller.tournament;

import com.tourney.dto.tournament.TournamentChallengeDTO;
import com.tourney.service.tournament.TournamentChallengeService;
import com.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/challenges")
@RequiredArgsConstructor
public class TournamentChallengeController {

    private final TournamentChallengeService challengeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TournamentChallengeDTO>> getChallenges(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(challengeService.getUserChallenges(tournamentId, userPrincipal.getId()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TournamentChallengeDTO> createChallenge(
            @PathVariable Long tournamentId,
            @RequestParam Long opponentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(challengeService.createChallenge(tournamentId, userPrincipal.getId(), opponentId));
    }

    @PostMapping("/{challengeId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> acceptChallenge(
            @PathVariable Long tournamentId,
            @PathVariable Long challengeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        challengeService.acceptChallenge(tournamentId, challengeId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{challengeId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> rejectChallenge(
            @PathVariable Long tournamentId,
            @PathVariable Long challengeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        challengeService.rejectChallenge(tournamentId, challengeId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{challengeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelChallenge(
            @PathVariable Long tournamentId,
            @PathVariable Long challengeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        challengeService.cancelChallenge(tournamentId, challengeId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
