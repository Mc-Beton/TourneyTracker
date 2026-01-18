package com.tourney.dto.matches;

import com.tourney.domain.games.MatchMode;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class SingleMatchResponseDTO {
    private Long matchId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String matchName;

    private Long gameSystemId;
    private String gameSystemName;

    private Long player1Id;
    private String player1Name;
    private Integer player1ready;

    private Long player2Id;
    private String player2Name;
    private Integer player2ready;

    private boolean hotSeat;
    private MatchMode mode;
}