package com.tourney.dto.scores;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoundScoreDTO {
    private Long roundId;
    private int roundNumber;
    private Long score;
}

