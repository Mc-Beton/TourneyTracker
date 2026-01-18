package com.tourney.dto.matches;

import com.tourney.domain.scores.ScoreType;
import com.tourney.dto.rounds.RoundTableRowDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MatchSummaryDTO {
    private Long matchId;
    private String matchName;

    private String player1Name;
    private String player2Name; // dla hotseat: guestPlayer2Name

    private String primaryMission;
    private String deployment;
    private Integer armyPower;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean ready;
    private boolean opponentReady;

    private List<RoundTableRowDTO> rounds;
    private Map<String, Map<ScoreType, Integer>> totalsByPlayerAndType;
    private Map<String, Integer> totalPointsByPlayer;

    private boolean primaryScoreEnabled;
    private boolean secondaryScoreEnabled;
    private boolean thirdScoreEnabled;
    private boolean additionalScoreEnabled;
}