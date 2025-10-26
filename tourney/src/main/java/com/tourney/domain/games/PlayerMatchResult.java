package com.tourney.domain.games;

import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Embeddable
@Getter
@Setter
public class PlayerMatchResult {
    @ElementCollection
    private Map<ScoreType, Double> scores = new HashMap<>();
    
    @ElementCollection
    private List<RoundScore> roundScores = new ArrayList<>();

    public void addScore(ScoreType type, Double value) {
        scores.put(type, value);
    }

    public void addRoundScore(int roundNumber, Map<ScoreType, Double> roundScores) {
        this.roundScores.add(new RoundScore(roundNumber, roundScores));
    }

    public double getTotalScore() {
        return scores.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public double getScoreByType(ScoreType type) {
        return scores.getOrDefault(type, 0.0);
    }

    @Embeddable
    @Getter
    @Setter
    public static class RoundScore {
        private int roundNumber;
        
        @ElementCollection
        private Map<ScoreType, Double> scores = new HashMap<>();

        public RoundScore() {
        }

        public RoundScore(int roundNumber, Map<ScoreType, Double> scores) {
            this.roundNumber = roundNumber;
            this.scores = new HashMap<>(scores);
        }

        public double getTotalScore() {
            return scores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
        }
    }
}