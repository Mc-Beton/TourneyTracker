package com.tourney.domain.participant;

public enum ArmyListStatus {
    NOT_SUBMITTED,  // Nie podano (szary)
    PENDING,        // Podano, czeka na zatwierdzenie (żółty)
    APPROVED,       // Zatwierdzony (zielony)
    REJECTED        // Odrzucony do poprawy (czerwony)
}
