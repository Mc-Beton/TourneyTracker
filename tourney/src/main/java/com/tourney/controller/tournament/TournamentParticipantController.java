package com.tourney.controller.tournament;

import com.common.security.UserPrincipal;
import com.tourney.dto.participant.ArmyListDetailsDTO;
import com.tourney.dto.participant.ReviewArmyListDTO;
import com.tourney.dto.participant.SubmitArmyListDTO;
import com.tourney.dto.participant.TournamentParticipantDTO;
import com.tourney.service.tournament.TournamentParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/participants")
@RequiredArgsConstructor
public class TournamentParticipantController {

    private final TournamentParticipantService participantService;

    /**
     * Uczestnik: Dodaj/edytuj rozpiskę armii
     */
    @PostMapping("/my-army-list")
    public ResponseEntity<TournamentParticipantDTO> submitArmyList(
            @PathVariable Long tournamentId,
            @RequestBody SubmitArmyListDTO dto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TournamentParticipantDTO result = participantService.submitArmyList(
                tournamentId,
                currentUser.getId(),
                dto
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Uczestnik: Pobierz szczegóły mojej rozpiski
     */
    @GetMapping("/my-army-list")
    public ResponseEntity<ArmyListDetailsDTO> getMyArmyList(
            @PathVariable Long tournamentId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ArmyListDetailsDTO result = participantService.getArmyListDetails(
                tournamentId,
                currentUser.getId()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Organizator: Pobierz szczegóły rozpiski uczestnika
     */
    @GetMapping("/{userId}/army-list")
    public ResponseEntity<ArmyListDetailsDTO> getParticipantArmyList(
            @PathVariable Long tournamentId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ArmyListDetailsDTO result = participantService.getArmyListDetailsAsOrganizer(
                tournamentId,
                userId,
                currentUser.getId()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Organizator: Zatwierdź lub odrzuć rozpiskę
     */
    @PostMapping("/{userId}/army-list/review")
    public ResponseEntity<TournamentParticipantDTO> reviewArmyList(
            @PathVariable Long tournamentId,
            @PathVariable Long userId,
            @RequestBody ReviewArmyListDTO dto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TournamentParticipantDTO result = participantService.reviewArmyList(
                tournamentId,
                userId,
                dto,
                currentUser.getId()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Organizator: Przełącz status płatności
     */
    @PatchMapping("/{userId}/payment")
    public ResponseEntity<TournamentParticipantDTO> togglePaymentStatus(
            @PathVariable Long tournamentId,
            @PathVariable Long userId,
            @RequestParam boolean isPaid,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TournamentParticipantDTO result = participantService.setPaymentStatus(
                tournamentId,
                userId,
                isPaid,
                currentUser.getId()
        );
        return ResponseEntity.ok(result);
    }
}
