package com.tourney.dto.tournament;

import com.tourney.domain.tournament.ChallengeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TournamentChallengeDTO {
    private Long id;
    private Long tournamentId;
    private Long challengerId;
    private String challengerName;
    private Long opponentId;
    private String opponentName;
    private ChallengeStatus status;
    private LocalDateTime createdAt;
}
