package com.tourney.dto.systems.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateArmyDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Army faction ID is required")
    private Long armyFactionId;
}
