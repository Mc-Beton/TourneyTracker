package com.tourney.dto.tournament;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipantStatsDTO {
    private Long userId;
    private String userName;
    private int wins;
    private int draws;
    private int losses;
    private int tournamentPoints; // suma dużych punktów
    private long scorePoints; // suma małych punktów
    private int matchesPlayed;
}
