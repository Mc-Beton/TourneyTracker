package com.tourney.dto.league;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class LeagueChallengeDTO {
    private Long id;
    private Long leagueId;
    private Long challengerId;
    private String challengerName;
    private Long challengedId;
    private String challengedName;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime scheduledTime;
    private String message;
    private Long matchId;
}
