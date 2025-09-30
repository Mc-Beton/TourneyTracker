package com.tourney.exception;

public class TournamentNotFoundException extends RuntimeException {
    public TournamentNotFoundException(Long tournamentId) {
        super("Nie znaleziono turnieju o ID: " + tournamentId);
    }
}