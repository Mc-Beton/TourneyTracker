package com.tourney.exception.domain;

public enum MatchErrorCode {
    MATCH_NOT_FOUND("Match.001", "Nie znaleziono meczu"),
    PLAYER_NOT_IN_MATCH("Match.002", "Gracz nie jest uczestnikiem tego meczu"),
    INVALID_MATCH_STATUS("Match.003", "Nieprawidłowy status meczu"),
    RESULTS_NOT_SUBMITTED("Match.004", "Wyniki meczu nie zostały jeszcze wprowadzone"),
    PLAYER_ALREADY_READY("Match.005", "Gracz już zgłosił gotowość"),
    OPPONENT_NOT_READY("Match.006", "Przeciwnik nie jest jeszcze gotowy"),
    MATCH_ALREADY_STARTED("Match.007", "Mecz już się rozpoczął"),
    MATCH_ALREADY_FINISHED("Match.008", "Mecz już się zakończył"),
    RESULT_ALREADY_CONFIRMED("Match.009", "Wynik został już potwierdzony"),
    INVALID_RESULT_CONFIRMATION("Match.010", "Nieprawidłowe potwierdzenie wyniku");

    private final String code;
    private final String defaultMessage;

    MatchErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}