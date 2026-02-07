package com.tourney.dto.participant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tourney.domain.participant.ArmyListStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TournamentParticipantDTO {
    private Long userId;
    private String name;
    private String email;
    private boolean confirmed;
    @JsonProperty("isPaid")
    private boolean isPaid;
    private ArmyListStatus armyListStatus;
    private String armyFactionName;
    private String armyName;
}

