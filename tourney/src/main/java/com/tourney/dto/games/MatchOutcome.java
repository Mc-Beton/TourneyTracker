package com.tourney.dto.games;

import lombok.Getter;

@Getter
public enum MatchOutcome {
    WIN("Wygrana"),
    LOSS("Przegrana"),
    DRAW("Remis"),
    IN_PROGRESS("W trakcie");

    private final String displayName;

    MatchOutcome(String displayName) {
        this.displayName = displayName;
    }

}