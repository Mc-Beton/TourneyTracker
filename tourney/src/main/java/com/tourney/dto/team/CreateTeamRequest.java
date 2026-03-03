package com.tourney.dto.team;

import lombok.Data;

@Data
public class CreateTeamRequest {
    private String name;
    private String abbreviation;
    private String city;
    private String description;
    private Long gameSystemId;
}
