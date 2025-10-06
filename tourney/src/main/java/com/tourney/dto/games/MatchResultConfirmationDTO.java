package com.tourney.dto.games;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchResultConfirmationDTO {
    private Long matchId;
    private boolean isConfirmed;
    private LocalDateTime completionTime;
}

