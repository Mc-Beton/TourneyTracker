package com.tourney.exception;

public class RoundNotCompletedException extends RuntimeException {
    public RoundNotCompletedException(Long tournamentId, int roundNumber) {
        super("Runda " + roundNumber + " w turnieju " + tournamentId + 
              " nie została zakończona. Wszystkie mecze muszą mieć wprowadzone wyniki.");
    }
}