package com.tourney.dto.tournament;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PodiumDTO {
    private ParticipantStatsDTO first;
    private ParticipantStatsDTO second;
    private ParticipantStatsDTO third;
}
