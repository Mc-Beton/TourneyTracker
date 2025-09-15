package com.tourney.dto.games;

import com.tourney.domain.games.MatchResult;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MatchDTO {
    private Long id;
    private LocalDateTime startTime;
    private int gameDurationMinutes;
    private LocalDateTime resultSubmissionDeadline;
    private Long roundId;
    private Long player1Id;
    private Long player2Id;
    private List<MatchRoundDTO> rounds;
    private MatchResult matchResult;
}