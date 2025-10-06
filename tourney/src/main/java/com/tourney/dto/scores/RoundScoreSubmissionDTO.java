package com.tourney.dto.scores;

import com.tourney.domain.scores.ScoreType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class RoundScoreSubmissionDTO {
    @NotNull
    private Long matchRoundId;

    @NotNull
    private Long userId;

    @NotNull
    private Map<ScoreType, Long> scores;
}

