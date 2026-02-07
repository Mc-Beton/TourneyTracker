package com.tourney.dto.tournament;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TournamentRoundViewDTO {
    private int roundNumber;
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED
    private List<MatchPairDTO> matches;
    private boolean canStart; // czy organizator może rozpocząć tę rundę
}
