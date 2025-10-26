package com.tourney.domain.games;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Embeddable
@Data
public class MatchResult {
    private Integer pointsParticipant1;
    private Integer pointsParticipant2;

    private Integer bigPointsParticipant1;
    private Integer bigPointsParticipant2;

    private Long winnerId;
    
    // Dodajemy pola do śledzenia, kto wprowadził wyniki
    private Long submittedById;
    private LocalDateTime submissionTime;

    public boolean hasPlayerSubmittedResults(Long playerId) {
        return submittedById != null && submittedById.equals(playerId);
    }

    public boolean isSubmittedByOpponent(Long playerId) {
        return submittedById != null && !submittedById.equals(playerId);
    }

    public void setSubmittedBy(Long playerId) {
        this.submittedById = playerId;
        this.submissionTime = LocalDateTime.now();
    }

    // Pomocnicza metoda do sprawdzenia czy wyniki zostały w ogóle wprowadzone
    public boolean hasResults() {
        return pointsParticipant1 != null && 
               pointsParticipant2 != null && 
               submittedById != null;
    }
}