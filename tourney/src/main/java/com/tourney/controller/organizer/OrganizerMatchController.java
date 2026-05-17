package com.tourney.controller.organizer;

import com.common.security.UserPrincipal;
import com.tourney.dto.matches.MatchScoringDTO;
import com.tourney.dto.scores.AdminBulkEditScoresDTO;
import com.tourney.service.organizer.OrganizerMatchAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizer/matches")
@RequiredArgsConstructor
public class OrganizerMatchController {

    private final OrganizerMatchAdminService organizerMatchAdminService;

    /**
     * Returns scoring view for a finished tournament match for the organizer.
     */
    @GetMapping("/{matchId}/scoring")
    public ResponseEntity<MatchScoringDTO> getScoring(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        MatchScoringDTO dto = organizerMatchAdminService.getScoringForOrganizer(matchId, currentUser.getId());
        return ResponseEntity.ok(dto);
    }

    /**
     * Bulk update of all rounds' scores for a finished tournament match.
     */
    @PutMapping("/{matchId}/scores")
    public ResponseEntity<MatchScoringDTO> updateScores(
            @PathVariable Long matchId,
            @RequestBody AdminBulkEditScoresDTO payload,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        MatchScoringDTO dto = organizerMatchAdminService.updateScoresBulk(matchId, currentUser.getId(), payload);
        return ResponseEntity.ok(dto);
    }
}
