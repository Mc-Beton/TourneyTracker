package com.tourney.domain.games;

import jakarta.persistence.*;

@Embeddable
public class MatchResult {
    private Integer pointsParticipant1;
    private Integer pointsParticipant2;

    private Integer bigPointsParticipant1;
    private Integer bigPointsParticipant2;
}
