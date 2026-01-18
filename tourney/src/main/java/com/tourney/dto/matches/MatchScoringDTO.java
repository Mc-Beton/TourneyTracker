// MatchScoringDTO.java
package com.tourney.dto.matches;

import com.tourney.dto.rounds.RoundScoresDTO;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MatchScoringDTO {
    private Long matchId;
    private String matchName;
    private String player1Name;
    private String player2Name;
    private int currentRound;
    private int totalRounds;

    private boolean primaryScoreEnabled;
    private boolean secondaryScoreEnabled;
    private boolean thirdScoreEnabled;
    private boolean additionalScoreEnabled;

    private List<RoundScoresDTO> rounds;
}