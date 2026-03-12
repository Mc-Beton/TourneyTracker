package com.tourney.dto.systems.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDeploymentDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Game system ID is required")
    private Long gameSystemId;
}
