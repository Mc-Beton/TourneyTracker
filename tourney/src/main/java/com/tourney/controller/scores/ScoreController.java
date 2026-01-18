package com.tourney.controller.scores;

import com.tourney.dto.scores.RoundScoreSubmissionDTO;
import com.tourney.service.scores.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {
    private final ScoreService scoringService;

    @PostMapping("/round")
    public ResponseEntity<Void> submitRoundScores(
            @RequestBody RoundScoreSubmissionDTO submissionDTO,
            @RequestHeader(name = "X-User-Id") Long currentUserId
    ) {
        scoringService.submitRoundScores(submissionDTO, currentUserId);
        return ResponseEntity.ok().build();
    }
}