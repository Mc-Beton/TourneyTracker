package com.tourney.domain.systems;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class StandardGameSystem extends GameSystem {
    // Możemy dodać specyficzne pola dla tego systemu gry
    private int basePointsForWin;
    private int basePointsForDraw;
}

