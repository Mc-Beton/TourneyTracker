package com.tourney.domain.scores;

public enum TournamentPointsSystem {
    FIXED("Stała punktacja"),
    POINT_DIFFERENCE_STRICT("Różnica punktowa - standardowa"),
    POINT_DIFFERENCE_LENIENT("Różnica punktowa - łagodna");

    private final String description;

    TournamentPointsSystem(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
