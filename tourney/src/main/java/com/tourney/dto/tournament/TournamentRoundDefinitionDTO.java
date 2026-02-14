package com.tourney.dto.tournament;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TournamentRoundDefinitionDTO {
    private Long id;
    private Integer roundNumber;
    private Long deploymentId;
    private String deploymentName;
    private Long primaryMissionId;
    private String primaryMissionName;
    private Boolean isSplitMapLayout;
    private String mapLayoutEven;
    private String mapLayoutOdd;
    private Integer byeLargePoints;
    private Integer byeSmallPoints;
    private Integer splitLargePoints;
    private Integer splitSmallPoints;
    private String pairingAlgorithm;
    private String playerLevelPairingStrategy;
    private String tableAssignmentStrategy;
}
