package com.tourney.domain.games;

import com.tourney.domain.tournament.TournamentRound;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Mecz turniejowy - część turnieju Swiss
 */
@Entity
@Getter
@Setter
@DiscriminatorValue("TOURNAMENT")
public class TournamentMatch extends Match {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private TournamentRound tournamentRound;
    
    /**
     * Czy mecz jest częścią turnieju
     */
    public boolean isTournamentMatch() {
        return true;
    }
}
