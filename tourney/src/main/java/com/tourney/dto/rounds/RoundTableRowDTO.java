package com.tourney.dto.rounds;

import com.tourney.domain.scores.ScoreType;
import lombok.*;

import java.util.Map;

@Data
@Builder
public class RoundTableRowDTO {
    private int roundNumber;

    /** Wyniki gracza 1 w tej rundzie */
    private Map<ScoreType, Integer> player1;

    /** Wyniki gracza 2 w tej rundzie (dla hotseat nadal wypełniane po side) */
    private Map<ScoreType, Integer> player2;
}