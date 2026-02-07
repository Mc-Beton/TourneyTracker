package com.tourney.dto.matches;

import com.tourney.domain.games.MatchStatus;
import com.tourney.dto.scores.RoundScoreDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CurrentMatchDTO {
    private Long matchId;
    private Integer tableNumber;
    private String opponentName;
    private MatchStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isReady; // czy gracz zgłosił gotowość
    private boolean opponentReady; // czy przeciwnik zgłosił gotowość
    private boolean resultsSubmitted; // czy wyniki zostały wprowadzone
    private boolean resultsConfirmed; // czy wyniki zostały potwierdzone
    private List<RoundScoreDTO> rounds;
}

