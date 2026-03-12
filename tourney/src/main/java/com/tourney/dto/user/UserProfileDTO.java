package com.tourney.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String name;
    private String realName;
    private String surname;
    private String email;
    private String team;
    private String city;
    private String discordNick;
    
    // Statistics
    private int totalMatches;
    private int wins;
    private int losses;
    private int draws;
    private double winRatio; // Calculated as (wins + 0.5 * draws) / totalMatches
}
