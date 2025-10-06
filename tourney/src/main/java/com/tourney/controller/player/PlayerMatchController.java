package com.tourney.controller.player;

import com.tourney.domain.user.User;
import com.tourney.dto.matches.CurrentMatchDTO;
import com.tourney.dto.matches.MatchStatusDTO;
import com.tourney.dto.player.OpponentStatusDTO;
import com.tourney.dto.tournament.ActiveTournamentDTO;
import com.tourney.service.player.PlayerMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerMatchController {
    private final PlayerMatchService playerMatchService;

    @GetMapping("/tournaments/active")
    public ResponseEntity<List<ActiveTournamentDTO>> getActiveTournaments(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(playerMatchService.getActiveTournaments(currentUser.getId()));
    }

    @GetMapping("/tournaments/{tournamentId}/current-match")
    public ResponseEntity<CurrentMatchDTO> getCurrentMatch(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(playerMatchService.getCurrentMatch(tournamentId, currentUser.getId()));
    }

    @PostMapping("/matches/{matchId}/report-ready")
    public ResponseEntity<MatchStatusDTO> reportReady(
            @PathVariable Long matchId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(playerMatchService.reportPlayerReady(matchId, currentUser.getId()));
    }

    @PostMapping("/matches/{matchId}/confirm-opponent-result")
    public ResponseEntity<MatchResultConfirmationDTO> confirmOpponentResult(
            @PathVariable Long matchId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(playerMatchService.confirmOpponentResult(matchId, currentUser.getId()));
    }

    @GetMapping("/matches/{matchId}/opponent-status")
    public ResponseEntity<OpponentStatusDTO> getOpponentStatus(
            @PathVariable Long matchId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(playerMatchService.getOpponentStatus(matchId, currentUser.getId()));
    }
}

