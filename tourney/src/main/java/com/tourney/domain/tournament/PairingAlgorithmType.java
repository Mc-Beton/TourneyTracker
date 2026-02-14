package com.tourney.domain.tournament;

/**
 * Typ algorytmu parowania graczy w rundzie turnieju
 */
public enum PairingAlgorithmType {
    /**
     * Standardowy algorytm - losowe przetasowanie i sekwencyjne parowanie
     */
    STANDARD,
    
    /**
     * Niestandardowy algorytm - konfigurowalny przez organizatora
     */
    CUSTOM
}
