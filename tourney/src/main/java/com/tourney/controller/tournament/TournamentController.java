package com.tourney.controller.tournament;

import com.common.security.UserPrincipal;
import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.tournament.*;
import com.tourney.mapper.tournament.TournamentMapper;
import com.tourney.service.tournament.TournamentManagementService;
import com.tourney.service.tournament.TournamentStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.common.domain.User;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    private final TournamentManagementService tournamentManagementService;
    private final TournamentMapper tournamentMapper;
    private final TournamentStatsService tournamentStatsService;

    @GetMapping
    public ResponseEntity<Iterable<TournamentResponseDTO>> getAllTournaments() {
        List<Tournament> tournamentList = tournamentManagementService.getActiveTournaments();
        return new ResponseEntity<>(tournamentMapper.getDtloList(tournamentList), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> getTournamentById(@PathVariable Long id) {
        Tournament tournament = tournamentManagementService.getTournamentById(id);
        return ResponseEntity.ok(tournamentMapper.toDto(tournament));
    }

    @PostMapping
    public ResponseEntity<TournamentResponseDTO> createTournament(
            @Valid @RequestBody CreateTournamentDTO createTournamentDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Tournament tournament = tournamentManagementService.createTournament(createTournamentDTO, currentUser.getId());
        return new ResponseEntity<>(tournamentMapper.toDto(tournament), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TournamentResponseDTO> updateTournament(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTournamentDTO updateTournamentDTO,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Tournament tournament = tournamentManagementService.updateTournament(id, updateTournamentDTO, currentUser.getId());
        return ResponseEntity.ok(tournamentMapper.toDto(tournament));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        tournamentManagementService.deleteTournament(id, currentUser.getId());
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

    @GetMapping("/mine")
    public ResponseEntity<List<TournamentResponseDTO>> getMyCreatedTournaments(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Long organizerId = currentUser.getId();
        List<Tournament> tournaments = tournamentManagementService.getTournamentsCreatedBy(organizerId);
        List<TournamentResponseDTO> dtos = tournaments.stream()
                .map(tournamentMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/edit")
    public ResponseEntity<CreateTournamentDTO> getTournamentEditForm(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        CreateTournamentDTO form = tournamentManagementService.getTournamentEditForm(id, currentUser.getId());
        return ResponseEntity.ok(form);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<TournamentResponseDTO> setTournamentActive(
            @PathVariable Long id,
            @RequestParam boolean active,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Tournament tournament = tournamentManagementService.setTournamentActive(id, active, currentUser.getId());
        return ResponseEntity.ok(tournamentMapper.toDto(tournament));
    }
    @GetMapping("/{id}/participants/stats")
    public ResponseEntity<List<ParticipantStatsDTO>> getParticipantStats(
            @PathVariable Long id
    ) {
        Tournament tournament = tournamentManagementService.getTournamentById(id);
        List<ParticipantStatsDTO> stats = tournamentStatsService.calculateParticipantStats(tournament);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/podium")
    public ResponseEntity<PodiumDTO> getPodium(
            @PathVariable Long id
    ) {
        Tournament tournament = tournamentManagementService.getTournamentById(id);
        PodiumDTO podium = tournamentStatsService.calculatePodium(tournament);
        return ResponseEntity.ok(podium);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<TournamentResponseDTO> startTournament(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Tournament tournament = tournamentManagementService.startTournament(id, currentUser.getId());
        return ResponseEntity.ok(tournamentMapper.toDto(tournament));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<TournamentResponseDTO> completeTournament(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Tournament tournament = tournamentManagementService.completeTournament(id, currentUser.getId());
        return ResponseEntity.ok(tournamentMapper.toDto(tournament));
    }


}