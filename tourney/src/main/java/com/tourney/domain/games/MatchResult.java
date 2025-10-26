package com.tourney.domain.games;

import com.tourney.domain.scores.Score;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Embeddable
@Getter
@Setter
public class MatchResult {
    private Long submittedById;
    private LocalDateTime submissionTime;
    private Long winnerId;

    @ElementCollection
    private Map<Long, PlayerMatchResult> playerResults = new HashMap<>();

    public void addPlayerResult(Long playerId, PlayerMatchResult result) {
        playerResults.put(playerId, result);
    }

    public PlayerMatchResult getPlayerResult(Long playerId) {
        return playerResults.get(playerId);
    }

    public double getPlayerScore(Long playerId) {
        PlayerMatchResult result = playerResults.get(playerId);
        return result != null ? result.getTotalScore() : 0.0;
    }

    public boolean hasPlayerSubmittedResults(Long playerId) {
        return submittedById != null && submittedById.equals(playerId);
    }

    public boolean isSubmittedByOpponent(Long playerId) {
        return submittedById != null && !submittedById.equals(playerId);
    }

    public void calculateWinner() {
        if (playerResults.size() != 2) {
            return;
        }

        double maxScore = -1;
        Long winningPlayerId = null;

        for (Map.Entry<Long, PlayerMatchResult> entry : playerResults.entrySet()) {
            double score = entry.getValue().getTotalScore();
            if (score > maxScore) {
                maxScore = score;
                winningPlayerId = entry.getKey();
            } else if (score == maxScore) {
                winningPlayerId = null; // Remis
            }
        }

        this.winnerId = winningPlayerId;
    }
}