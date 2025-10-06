package com.tourney.exception;

public class TournamentException extends RuntimeException {
    private final TournamentErrorCode errorCode;

    public TournamentException(TournamentErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

