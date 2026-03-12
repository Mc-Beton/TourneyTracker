package com.tourney.dto.systems.admin;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateGameSystemDTO {
    private String name;

    @Min(value = 1, message = "Default round number must be at least 1")
    private Integer defaultRoundNumber;

    private Boolean primaryScoreEnabled;
    private Boolean secondaryScoreEnabled;
    private Boolean thirdScoreEnabled;
    private Boolean additionalScoreEnabled;
}
