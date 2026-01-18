package com.tourney.dto.matches;

import com.tourney.domain.games.MatchMode;
import com.tourney.dto.scores.RoundScoreDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MatchDetailsDTO {
    private Long matchId;
    private LocalDateTime startTime;
    private String matchName;
    private MatchMode mode;
    private Long opponentId;
    private String opponentName;
    private List<RoundScoreDTO> roundScores;
}