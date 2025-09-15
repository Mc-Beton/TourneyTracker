package com.tourney.domain.scores;

public enum ScoreType {

    MAIN_SCORE(1L,"Main Score"),
    SECONDARY_SCORE(2L,"Secondary Score"),
    THIRD_SCORE(3L,"Third Score"),
    ADDITIONAL_SCORE(4L,"Additional Score");

    private final Long id;
    private final String nameType;

    ScoreType(Long id, String nameType) {
        this.id = id;
        this.nameType = nameType;
    }

    public Long getId() {
        return id;
    }

    public String getNameType() {
        return nameType;
    }
}
