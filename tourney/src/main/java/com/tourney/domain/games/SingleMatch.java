package com.tourney.domain.games;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * Pojedyncza rozgrywka (poza turniejem)
 */
@Entity
@Getter
@Setter
@DiscriminatorValue("SINGLE")
public class SingleMatch extends Match {
    
    /**
     * Czy mecz jest częścią turnieju
     */
    public boolean isTournamentMatch() {
        return false;
    }
}
