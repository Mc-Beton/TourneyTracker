// RoundScoresDTO.java
package com.tourney.dto.rounds;

import com.tourney.domain.games.RoundStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RoundScoresDTO {
    private int roundNumber;
    private Long player1MainScore;
    private Long player1SecondaryScore;
    private Long player2MainScore;
    private Long player2SecondaryScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private RoundStatus status;
}