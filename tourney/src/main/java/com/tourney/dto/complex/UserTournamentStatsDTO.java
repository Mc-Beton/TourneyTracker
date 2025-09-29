package com.tourney.dto.complex;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTournamentStatsDTO {
    private Long userId;
    private String userName;
    private Long totalScore;
    private int matchId;
}