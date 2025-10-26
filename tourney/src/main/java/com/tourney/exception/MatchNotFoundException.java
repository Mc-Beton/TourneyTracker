package com.tourney.exception;

public class MatchNotFoundException extends RuntimeException {
    public MatchNotFoundException(Long matchId) {
        super("Nie znaleziono meczu o ID: " + matchId);
    }
}