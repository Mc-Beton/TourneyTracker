package com.tourney.dto.league;

import com.tourney.dto.systems.GameSystemDTO;
import com.tourney.dto.user.UserDTO;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeagueDTO {

    private Long id;
    private String name;
    private String description;
    private GameSystemDTO gameSystem;
    private UserDTO owner;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private boolean autoAcceptGames;
    private boolean autoAcceptTournaments;
    private boolean paymentRequired;
    private int pointsWin;
    private int pointsDraw;
    private int pointsLoss;
    private int pointsParticipation;
    private int pointsPerParticipant;
    private int pointsFirstPlace;
    private int pointsSecondPlace;
    private int pointsThirdPlace;

    // Optional counts
    private int memberCount;
}
