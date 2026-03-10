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
     * Algorytm backtrackingowy - systematycznie przeszukuje wszystkie możliwe parowania,
     * gwarantuje znalezienie rozwiązania jeśli istnieje. Uwzględnia ograniczenia miękkie (drużyny, miasta)
     */
    BACKTRACKING,
    
    /**
     * Algorytm CP-SAT (Constraint Programming - Satisfiability) - używa solvera optymalizacyjnego
     * do znalezienia optymalnego parowania z wagami dla ograniczeń miękkich
     */
    CP_SAT
}
