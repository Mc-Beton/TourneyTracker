package com.tourney.dto.league;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateChallengeDTO {
    private Long opponentId;
    private LocalDateTime scheduledTime;
    private String message;
}
