package com.tourney.dto.scores;

import com.tourney.domain.scores.ScoreType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreDTO {
    private Long id;
    private Long matchRoundId;
    private Long userId;
    private ScoreType scoreType;
    private Long score;
}