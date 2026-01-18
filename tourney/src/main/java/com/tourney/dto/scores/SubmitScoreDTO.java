// SubmitScoreDTO.java
package com.tourney.dto.scores;

import lombok.Data;
import java.util.List;

@Data
public class SubmitScoreDTO {
    private int roundNumber;
    private List<ScoreEntryDTO> scores;
}