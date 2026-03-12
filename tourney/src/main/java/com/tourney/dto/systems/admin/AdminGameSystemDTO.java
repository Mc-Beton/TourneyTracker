package com.tourney.dto.systems.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGameSystemDTO {
    private Long id;
    private String name;
    private Integer defaultRoundNumber;
    private Boolean primaryScoreEnabled;
    private Boolean secondaryScoreEnabled;
    private Boolean thirdScoreEnabled;
    private Boolean additionalScoreEnabled;
}
