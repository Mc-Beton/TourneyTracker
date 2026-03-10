package com.tourney.dto.league;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateLeagueDTO {

    private String name;
    private String description;
    private Long gameSystemId;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean autoAcceptGames;
    private boolean autoAcceptTournaments;
    private int pointsWin;
    private int pointsDraw;
    private int pointsLoss;
    private int pointsParticipation;
    private int pointsPerParticipant;
    private int pointsFirstPlace;
    private int pointsSecondPlace;
    private int pointsThirdPlace;
}
