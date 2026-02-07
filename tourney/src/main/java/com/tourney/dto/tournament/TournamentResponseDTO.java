package com.tourney.dto.tournament;

import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.scores.ScoringSystem;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.tourney.dto.tournament.TournamentStatus;

@Data
@Builder
public class TournamentResponseDTO {
    private Long id;
    private String name;
    private LocalDate startDate;
    private int numberOfRounds;
    private int roundDurationMinutes;
    private Long gameSystemId;
    private String gameSystemName;
    private Long organizerId;
    private List<Long> participantIds;
    private String location;
    private String description;
    private Integer maxParticipants;
    private String organizerName;
    private Integer armyPointsLimit;
    private Integer confirmedParticipantsCount;

    private TournamentStatus status;

    // Dodane pola dotyczące punktacji
    private ScoringSystem scoringSystem;
    private Set<ScoreType> enabledScoreTypes;
    private boolean requireAllScoreTypes;
    private Integer minScore;
    private Integer maxScore;
}