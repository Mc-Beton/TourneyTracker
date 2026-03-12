package com.tourney.dto.systems.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGameSystemDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Default round number is required")
    @Min(value = 1, message = "Default round number must be at least 1")
    private Integer defaultRoundNumber;

    private Boolean primaryScoreEnabled = true;
    private Boolean secondaryScoreEnabled = true;
    private Boolean thirdScoreEnabled = false;
    private Boolean additionalScoreEnabled = false;
}
