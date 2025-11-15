package com.tourney.domain.games;

import com.tourney.domain.scores.ScoreType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;

import java.util.HashMap;
import java.util.Map;

@Embeddable
public class RoundScore {
    @ElementCollection
    private Map<ScoreType, Double> scores = new HashMap<>();

    public double getTotalScore() {
        return scores.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}

