package com.tourney.dto.team;

import lombok.Data;

@Data
public class UpdateTeamRequest {
    private String name;
    private String abbreviation;
    private String city;
    private String description;
}
