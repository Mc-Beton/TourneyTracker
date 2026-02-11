package com.tourney.domain.tournament;

/**
 * Wewnętrzny status postępu turnieju - szczegółowa maszyna stanów
 * dla procesu rozgrywania rund turnieju
 */
public enum TournamentPhase {
    /**
     * Czeka na dobranie par dla bieżącej rundy
     */
    AWAITING_PAIRINGS,
    
    /**
     * Pary dobrane, mecze utworzone, czeka na rozpoczęcie rundy
     */
    PAIRINGS_READY,
    
    /**
     * Runda aktywna - mecze w trakcie, licznik działa
     */
    ROUND_ACTIVE,
    
    /**
     * Runda zakończona - gotowy do dobierania par na następną rundę
     */
    ROUND_FINISHED,
    
    /**
     * Wszystkie rundy zakończone - turniej skończony
     */
    TOURNAMENT_COMPLETE
}
