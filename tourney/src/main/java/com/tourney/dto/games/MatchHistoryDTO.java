package com.tourney.dto.games;

import com.tourney.domain.games.MatchResult;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchHistoryDTO {
    private Long matchId;
    private int roundNumber;
    private String opponentName;
    private MatchOutcome outcome;
    private int playerScore;
    private int opponentScore;
    private LocalDateTime playedAt;
}

