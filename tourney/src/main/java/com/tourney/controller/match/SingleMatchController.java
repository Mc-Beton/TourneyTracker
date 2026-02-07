package com.tourney.controller.match;

import com.common.security.UserPrincipal;
import com.tourney.domain.games.SingleMatch;
import com.tourney.dto.matches.CreateSingleMatchDTO;
import com.tourney.dto.matches.MatchDetailsDTO;
import com.tourney.dto.matches.MatchSummaryDTO;
import com.tourney.dto.matches.SingleMatchResponseDTO;
import com.tourney.mapper.match.SingleMatchMapper;
import com.tourney.service.match.SingleMatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class SingleMatchController {

    private final SingleMatchService singleMatchService;
    private final SingleMatchMapper singleMatchMapper;

    @PostMapping("/single")
    public ResponseEntity<SingleMatchResponseDTO> createSingle(
            @Valid @RequestBody CreateSingleMatchDTO dto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        SingleMatch match = singleMatchService.createSingleMatch(dto, currentUser.getId());
        return ResponseEntity.ok(singleMatchMapper.toDto(match));
    }

    @GetMapping("/single/mine")
    public ResponseEntity<List<SingleMatchResponseDTO>> getMySingleMatches(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<SingleMatchResponseDTO> result = singleMatchService.getMySingleMatches(currentUser.getId())
                .stream()
                .map(singleMatchMapper::toDto)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{matchId}/details")
    public ResponseEntity<MatchDetailsDTO> getMatchDetails(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(singleMatchService.getMatchDetails(matchId, currentUser.getId()));
    }

    @GetMapping("/{matchId}/summary")
    public ResponseEntity<MatchSummaryDTO> getMatchSummary(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(singleMatchService.getMatchSummary(matchId, currentUser.getId()));
    }

    @PostMapping("/{matchId}/finish")
    public ResponseEntity<MatchSummaryDTO> finishMatch(
            @PathVariable Long matchId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(singleMatchService.finishMatch(matchId, currentUser.getId()));
    }
}