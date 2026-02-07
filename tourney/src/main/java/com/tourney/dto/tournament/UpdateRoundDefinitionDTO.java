package com.tourney.dto.tournament;

import lombok.Data;

@Data
public class UpdateRoundDefinitionDTO {
    private Long deploymentId;
    private Long primaryMissionId;
    private Boolean isSplitMapLayout;
    private String mapLayoutEven;
    private String mapLayoutOdd;
    private Integer byeLargePoints;
    private Integer byeSmallPoints;
    private Integer splitLargePoints;
    private Integer splitSmallPoints;
}
