package com.tourney.dto.tournament;

import com.tourney.domain.games.MatchStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActiveTournamentDTO {
    private Long tournamentId;
    private String tournamentName;
    private int currentRound;
    private LocalDateTime roundStartTime;
    private LocalDateTime roundEndTime;
    private MatchStatus currentMatchStatus;
    private String opponent;
    private boolean requiresAction; // czy gracz musi coś zrobić (np. zatwierdzić wynik)
}

