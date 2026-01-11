package com.tourney.controller.tournament;

import com.common.security.UserPrincipal;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.user.User;
import com.tourney.dto.complex.UserTournamentMatchesDTO;
import com.tourney.dto.participant.TournamentParticipantDTO;
import com.tourney.dto.tournament.TournamentResponseDTO;
import com.tourney.mapper.tournament.TournamentMapper;
import com.tourney.service.tournament.TournamentManagementService;
import com.tourney.service.tournament.TournamentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentUserController {
    private final TournamentUserService tournamentUserService;
    private final TournamentMapper tournamentMapper;

    @GetMapping("/{tournamentId}/users")
    public ResponseEntity<List<TournamentParticipantDTO>> getTournamentUsers(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(tournamentUserService.getParticipants(tournamentId));
    }

    @GetMapping("/{tournamentId}/users/stats")
    public ResponseEntity<List<UserTournamentMatchesDTO>> getTournamentUsersStats(
            @PathVariable Long tournamentId) {
        return ResponseEntity.ok(tournamentUserService.getUsersMatchesWithScores(tournamentId));
    }

    @GetMapping("/{tournamentId}/users/pending")
    public ResponseEntity<List<TournamentParticipantDTO>> getPendingTournamentUsers(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(tournamentUserService.getParticipantsByConfirmation(tournamentId, false));
    }

    @GetMapping("/{tournamentId}/users/confirmed")
    public ResponseEntity<List<TournamentParticipantDTO>> getConfirmedTournamentUsers(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(tournamentUserService.getParticipantsByConfirmation(tournamentId, true));
    }

    @PatchMapping("/{tournamentId}/participants/{userId}/confirm")
    public ResponseEntity<TournamentResponseDTO> confirmParticipant(
            @PathVariable Long tournamentId,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "true") boolean confirmed,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        var tournament = tournamentUserService.setParticipantConfirmation(tournamentId, userId, confirmed, currentUser.getId());
        return ResponseEntity.ok(tournamentMapper.toDto(tournament));
    }

}