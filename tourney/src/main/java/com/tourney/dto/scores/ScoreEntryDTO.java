// ScoreEntryDTO.java
package com.tourney.dto.scores;

import com.tourney.domain.scores.MatchSide;
import com.tourney.domain.scores.ScoreType;
import lombok.Data;

@Data
public class ScoreEntryDTO {
    private MatchSide side;
    private ScoreType scoreType;
    private Long score;
}