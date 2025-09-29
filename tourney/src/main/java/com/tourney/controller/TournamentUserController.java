package com.tourney.controller;

import com.tourney.domain.user.User;
import com.tourney.dto.complex.UserTournamentMatchesDTO;
import com.tourney.dto.complex.UserTournamentStatsDTO;
import com.tourney.service.tournament.TournamentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentUserController {
    private final TournamentUserService tournamentUserService;

    @GetMapping("/{tournamentId}/users")
    public ResponseEntity<List<User>> getTournamentUsers(@PathVariable Long tournamentId) {
        List<User> users = tournamentUserService.getUsersByTournamentId(tournamentId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{tournamentId}/users/stats")
    public ResponseEntity<List<UserTournamentMatchesDTO>> getTournamentUsersStats(
            @PathVariable Long tournamentId) {
        return ResponseEntity.ok(tournamentUserService.getUsersMatchesWithScores(tournamentId));
    }

}