package com.tourney.dto.scores;

import com.tourney.domain.scores.MatchSide;
import com.tourney.domain.scores.ScoreType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class RoundScoreSubmissionDTO {

    @NotNull
    private Long matchRoundId;

    @NotNull
    private MatchSide side;

    @NotNull
    private Map<ScoreType, Long> scores;
}