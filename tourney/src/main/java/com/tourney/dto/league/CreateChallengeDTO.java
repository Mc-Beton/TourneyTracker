package com.tourney.dto.league;

import com.tourney.domain.games.MatchMode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateChallengeDTO {
    private Long opponentId;
    private LocalDateTime scheduledTime;
    private String message;
    private MatchMode matchMode;
}
