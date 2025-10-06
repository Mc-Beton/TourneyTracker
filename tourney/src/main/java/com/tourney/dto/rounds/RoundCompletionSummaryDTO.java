package com.tourney.dto.rounds;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoundCompletionSummaryDTO {
    private int roundNumber;
    private int totalMatches;
    private int completedMatches;
    private int pendingMatches;
    private boolean isCompleted;
    private String statusMessage;
}

