package com.tourney.exception;

import com.tourney.exception.domain.MatchErrorCode;
import lombok.Getter;

public class MatchOperationException extends RuntimeException {
    @Getter
    private final MatchErrorCode errorCode;
    private final String message;

    public MatchOperationException(MatchErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage());
    }

    public MatchOperationException(MatchErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public MatchOperationException(MatchErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return String.format("[%s] %s", errorCode.getCode(), message);
    }
}