package com.tourney.dto.participant;

import com.tourney.domain.participant.ArmyListStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArmyListDetailsDTO {
    private Long userId;
    private String userName;
    private Long armyFactionId;
    private String armyFactionName;
    private Long armyId;
    private String armyName;
    private String armyListContent;
    private ArmyListStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
}
