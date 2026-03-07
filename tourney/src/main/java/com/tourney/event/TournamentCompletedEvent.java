package com.tourney.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a tournament is completed
 */
@Getter
public class TournamentCompletedEvent extends ApplicationEvent {
    private final Long tournamentId;
    
    public TournamentCompletedEvent(Object source, Long tournamentId) {
        super(source);
        this.tournamentId = tournamentId;
    }
}
