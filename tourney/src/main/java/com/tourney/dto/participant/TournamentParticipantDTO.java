package com.tourney.dto.participant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TournamentParticipantDTO {
    private Long userId;
    private String name;
    private String email;
    private boolean confirmed;
}

