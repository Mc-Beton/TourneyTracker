package com.tourney.dto.matches;

import com.tourney.domain.games.MatchMode;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSingleMatchDTO {

    @NotNull
    private Long gameSystemId;

    private Long player2Id;
    private String guestPlayer2Name;

    // Match details (opcjonalne):
    private Long primaryMissionId;
    private Long deploymentId;
    private Integer armyPower;
    private String matchName;

    private Long firstPlayerId;
    private MatchMode mode;
}