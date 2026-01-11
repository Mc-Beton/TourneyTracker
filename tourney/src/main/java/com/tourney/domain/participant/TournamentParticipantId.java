package com.tourney.domain.participant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class TournamentParticipantId implements Serializable {
    private Long tournamentId;
    private Long userId;
}

