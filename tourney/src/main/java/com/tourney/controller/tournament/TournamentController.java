package com.tourney.controller.tournament;

import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.tournament.CreateTournamentDTO;
import com.tourney.dto.tournament.TournamentResponseDTO;
import com.tourney.dto.tournament.UpdateTournamentDTO;
import com.tourney.mapper.tournament.TournamentMapper;
import com.tourney.service.tournament.TournamentManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.tourney.domain.user.User;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    private final TournamentManagementService tournamentManagementService;
    private final TournamentMapper tournamentMapper;

    @PostMapping
    public ResponseEntity<TournamentResponseDTO> createTournament(
            @Valid @RequestBody CreateTournamentDTO createTournamentDTO,
            @AuthenticationPrincipal User currentUser) {
        Tournament tournament = tournamentManagementService.createTournament(createTournamentDTO, currentUser.getId());
        return new ResponseEntity<>(tournamentMapper.toDto(tournament), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> updateTournament(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTournamentDTO updateTournamentDTO) {
        Tournament tournament = tournamentManagementService.updateTournament(id, updateTournamentDTO);
        return ResponseEntity.ok(tournamentMapper.toDto(tournament));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
        tournamentManagementService.deleteTournament(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tournamentId}/participants/{userId}")
    public ResponseEntity<TournamentResponseDTO> addParticipant(
            @PathVariable Long tournamentId,
            @PathVariable Long userId) {
        Tournament tournament = tournamentManagementService.addParticipant(tournamentId, userId);
        return ResponseEntity.ok(tournamentMapper.toDto(tournament));
    }

    @DeleteMapping("/{tournamentId}/participants/{userId}")
    public ResponseEntity<TournamentResponseDTO> removeParticipant(
            @PathVariable Long tournamentId,
            @PathVariable Long userId) {
        Tournament tournament = tournamentManagementService.removeParticipant(tournamentId, userId);
        return ResponseEntity.ok(tournamentMapper.toDto(tournament));
    }
}