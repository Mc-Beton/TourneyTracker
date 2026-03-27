package com.tourney.dto.league;

import com.tourney.domain.league.LeagueMemberStatus;
import com.tourney.dto.user.UserDTO;
import lombok.Data;

@Data
public class LeagueMemberDTO {

    private Long id;
    private Long leagueId;
    private UserDTO user;
    private LeagueMemberStatus status;
    private int points;
    private int matchesPlayed;
    private int wins;
    private int draws;
    private int losses;
    private int tournamentsPlayed;
    private int tournamentWins;
    private int pointsScored;
    private boolean hasPaid;
}
