package com.tourney.dto.participant;

import com.tourney.domain.participant.ArmyListStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitArmyListDTO {
    private Long armyFactionId;
    private Long armyId;
    private String armyListContent;
}
