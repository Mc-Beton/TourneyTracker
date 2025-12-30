package com.tourney.domain.systems;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StandardGameSystem {
    // Możemy dodać specyficzne pola dla tego systemu gry
    private int basePointsForWin;
    private int basePointsForDraw;
}

