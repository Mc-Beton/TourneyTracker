package com.tourney.dto.tournament;

import com.tourney.domain.games.MatchStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class ActiveTournamentDTO {
    private Long tournamentId;
    private String tournamentName;
    private int currentRound;
    private OffsetDateTime roundStartTime;
    private OffsetDateTime roundEndTime;
    private MatchStatus currentMatchStatus;
    private String opponent;
    private boolean requiresAction; // czy gracz musi coś zrobić (np. zatwierdzić wynik)
}

