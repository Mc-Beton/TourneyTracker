package com.tourney.dto.team;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TeamDTO {
    private Long id;
    private String name;
    private String abbreviation;
    private String city;
    private String description;
    private Long ownerId;
    private String ownerName;
    private Long gameSystemId;
    private String gameSystemName;
    private LocalDateTime createdAt;
    private int memberCount;
    @com.fasterxml.jackson.annotation.JsonProperty("isMember")
    private boolean isMember;
    @com.fasterxml.jackson.annotation.JsonProperty("isOwner")
    private boolean isOwner;
}
