package com.tourney.dto.participant;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewArmyListDTO {
    private boolean approved; // true = approve, false = reject
    private String rejectionReason; // tylko gdy approved = false
}
