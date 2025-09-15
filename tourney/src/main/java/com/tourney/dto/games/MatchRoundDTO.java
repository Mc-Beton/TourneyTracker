package com.tourney.dto.games;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchRoundDTO {
    private Long id;
    private Long matchId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}