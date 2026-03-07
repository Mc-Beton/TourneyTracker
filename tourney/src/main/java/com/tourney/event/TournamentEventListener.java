package com.tourney.event;

import com.tourney.service.league.LeagueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listener for tournament events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TournamentEventListener {
    
    private final LeagueService leagueService;
    
    /**
     * When a tournament is completed, automatically process league points if the tournament belongs to a league
     */
    @EventListener
    @Transactional
    public void handleTournamentCompleted(TournamentCompletedEvent event) {
        try {
            leagueService.processTournamentPoints(event.getTournamentId());
            log.info("Successfully processed league points for tournament ID: {}", event.getTournamentId());
        } catch (IllegalArgumentException e) {
            // Tournament not associated with a league, or already processed - this is OK
            log.debug("Tournament ID {} not processed for league points: {}", event.getTournamentId(), e.getMessage());
        } catch (Exception e) {
            // Log but don't fail the tournament completion
            log.error("Error processing league points for tournament ID: {}", event.getTournamentId(), e);
        }
    }
}
