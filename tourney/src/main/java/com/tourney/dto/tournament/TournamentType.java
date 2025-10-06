package com.tourney.dto.tournament;

public enum TournamentType {
    SWISS("System szwajcarski"),
    ROUND_ROBIN("Każdy z każdym"),
    SINGLE_ELIMINATION("Pojedyncza eliminacja"),
    DOUBLE_ELIMINATION("Podwójna eliminacja");

    private final String displayName;

    TournamentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

