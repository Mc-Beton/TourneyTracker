package com.tourney.dto.scores;

import com.tourney.domain.scores.ScoreType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RoundScoreDTO {
    private int roundNumber;
    private Map<ScoreType, Integer> playerScore;
    private Map<ScoreType, Integer> opponentScore;
    private boolean isSubmitted;
    private boolean isConfirmed;
}


