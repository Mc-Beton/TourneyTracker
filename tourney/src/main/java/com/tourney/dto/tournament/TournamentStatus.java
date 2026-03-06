package com.tourney.dto.tournament;

public enum TournamentStatus {
    PENDING("Oczekujący"),
    DRAFT("W przygotowaniu"),
    ACTIVE("Aktywny"),
    IN_PROGRESS("W trakcie"),
    COMPLETED("Zakończony"),
    CANCELLED("Anulowany");

    private final String displayName;

    TournamentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

