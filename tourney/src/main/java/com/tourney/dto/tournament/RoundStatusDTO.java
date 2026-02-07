package com.tourney.dto.tournament;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoundStatusDTO {
    private int roundNumber;
    private boolean allScoresSubmitted;
    private List<String> playersWithoutScores; // nazwy graczy którzy nie wpisali punktów
    private int totalMatches;
    private int completedMatches;
}
