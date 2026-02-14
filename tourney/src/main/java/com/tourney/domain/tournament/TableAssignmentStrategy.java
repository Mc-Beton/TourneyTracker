package com.tourney.domain.tournament;

/**
 * Strategia przypisywania numerów stołów do par w kolejnych rundach
 */
public enum TableAssignmentStrategy {
    /**
     * Najlepsi gracze do pierwszych stołów
     * Pary są numerowane sekwencyjnie według rankingu
     */
    BEST_FIRST,
    
    /**
     * Losowe przypisywanie numerów stołów
     * Każda para dostaje losowy numer stołu niezależnie od rankingu
     */
    RANDOM
}
