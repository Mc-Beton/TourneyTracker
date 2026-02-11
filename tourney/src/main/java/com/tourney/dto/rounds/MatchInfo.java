package com.tourney.dto.rounds;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchInfo {
    private Long matchId;
    private Integer tableNumber;
    private String player1Name;
    private String player2Name;
    private LocalDateTime startTime;
    private Integer durationMinutes;
}
