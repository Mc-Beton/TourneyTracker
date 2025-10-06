package com.tourney.dto.tournament;

import com.tourney.domain.scores.ScoreType;
import lombok.Data;

import java.util.Map;

@Data
public class TournamentStandingsDTO {
    private Long userId;
    private String userName;
    private int position;
    private int matchesPlayed;
    private int matchesWon;
    private int matchesDrawn;
    private int matchesLost;
    private long totalScore;
    private Map<ScoreType, Long> scoresByType;
    private List<TieBreaker> tieBreakers;
}