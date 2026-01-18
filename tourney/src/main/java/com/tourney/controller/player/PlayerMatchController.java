package com.tourney.controller.player;

import com.common.security.UserPrincipal;
import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.user.User;
import com.tourney.dto.games.MatchResultConfirmationDTO;
import com.tourney.dto.matches.CurrentMatchDTO;
import com.tourney.dto.matches.MatchStatusDTO;
import com.tourney.dto.player.OpponentStatusDTO;
import com.tourney.dto.tournament.ActiveTournamentDTO;
import com.tourney.repository.games.MatchRepository;
import com.tourney.service.player.PlayerMatchService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerMatchController {
    private final PlayerMatchService playerMatchService;

    @GetMapping("/tournaments/active")
    public ResponseEntity<List<ActiveTournamentDTO>> getActiveTournaments(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(playerMatchService.getActiveTournaments(currentUser.getId()));
    }

    @GetMapping("/tournaments/{tournamentId}/current-match")
    public ResponseEntity<CurrentMatchDTO> getCurrentMatch(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(playerMatchService.getCurrentMatch(tournamentId, currentUser.getId()));
    }

    @PostMapping("/matches/{matchId}/report-ready")
    public ResponseEntity<MatchStatusDTO> reportReady(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal currentUser  // Zmień User na UserPrincipal
    ) {
        return ResponseEntity.ok(playerMatchService.reportPlayerReady(matchId, currentUser.getId()));
    }

    @PostMapping("/matches/{matchId}/start")
    public ResponseEntity<MatchStatusDTO> startMatch(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(playerMatchService.startMatch(matchId, currentUser.getId()));
    }

    @PostMapping("/matches/{matchId}/confirm-opponent-result")
    public ResponseEntity<MatchResultConfirmationDTO> confirmOpponentResult(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(playerMatchService.confirmOpponentResult(matchId, currentUser.getId()));
    }

    @GetMapping("/matches/{matchId}/opponent-status")
    public ResponseEntity<OpponentStatusDTO> getOpponentStatus(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(playerMatchService.getOpponentStatus(matchId, currentUser.getId()));
    }
}