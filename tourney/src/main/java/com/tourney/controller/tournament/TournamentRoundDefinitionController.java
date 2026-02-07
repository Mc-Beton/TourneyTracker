package com.tourney.controller.tournament;

import com.common.security.UserPrincipal;
import com.tourney.dto.tournament.TournamentRoundDefinitionDTO;
import com.tourney.dto.tournament.UpdateRoundDefinitionDTO;
import com.tourney.service.tournament.TournamentRoundDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/round-definitions")
@RequiredArgsConstructor
public class TournamentRoundDefinitionController {
    
    private final TournamentRoundDefinitionService roundDefinitionService;
    
    @GetMapping
    public ResponseEntity<List<TournamentRoundDefinitionDTO>> getRoundDefinitions(
            @PathVariable Long tournamentId) {
        return ResponseEntity.ok(roundDefinitionService.getRoundDefinitions(tournamentId));
    }
    
    @PutMapping("/{roundNumber}")
    public ResponseEntity<TournamentRoundDefinitionDTO> updateRoundDefinition(
            @PathVariable Long tournamentId,
            @PathVariable Integer roundNumber,
            @RequestBody UpdateRoundDefinitionDTO dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(
                roundDefinitionService.updateRoundDefinition(tournamentId, roundNumber, dto, currentUser.getId())
        );
    }
}
