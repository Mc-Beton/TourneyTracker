package com.tourney.domain.tournament;

public enum RoundStartMode {
    ALL_MATCHES_TOGETHER("Wszystkie mecze startują jednocześnie"),
    INDIVIDUAL_MATCHES("Każdy mecz startuje osobno");

    private final String description;

    RoundStartMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
