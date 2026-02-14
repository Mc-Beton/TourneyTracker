package com.tourney.domain.tournament;

/**
 * Strategia parowania graczy według poziomu zaawansowania
 */
public enum PlayerLevelPairingStrategy {
    /**
     * Brak preferencji - paruj losowo bez względu na poziom
     */
    NONE,
    
    /**
     * Preferuj parowanie początkujących z weteranami
     * Jeśli możliwe, paruj beginners z veterans, reszta losowo
     */
    BEGINNERS_WITH_VETERANS,
    
    /**
     * Preferuj parowanie graczy tego samego poziomu
     * Unikaj mieszanych poziomów jeśli możliwe
     */
    BEGINNERS_WITH_BEGINNERS
}
