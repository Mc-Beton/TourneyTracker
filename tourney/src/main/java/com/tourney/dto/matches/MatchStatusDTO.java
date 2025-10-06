package com.tourney.dto.matches;

import com.tourney.domain.games.MatchStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchStatusDTO {
    private Long matchId;
    private MatchStatus status;
    private boolean player1Ready;
    private boolean player2Ready;
    private LocalDateTime lastStatusUpdate;
}

