package com.tourney.domain.scores;

public enum ScoringSystem {
    ROUND_BY_ROUND("Po każdej rundzie"),
    END_OF_MATCH("Na koniec meczu");

    private final String description;

    ScoringSystem(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}