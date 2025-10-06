package com.tourney.dto.player;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OpponentStatusDTO {
    private String opponentName;
    private boolean isReady;
    private boolean hasSubmittedResults;
    private LocalDateTime lastActivity;
}

