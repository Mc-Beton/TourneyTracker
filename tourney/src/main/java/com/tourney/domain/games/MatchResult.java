package com.tourney.domain.games;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
public class MatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long submittedById;
    private LocalDateTime submissionTime;
    private Long winnerId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
        name = "match_result_player_scores",
        joinColumns = @JoinColumn(name = "match_result_id"),
        inverseJoinColumns = @JoinColumn(name = "player_score_id")
    )
    @MapKeyColumn(name = "player_id") // Kolumna przechowująca klucz mapy (Long playerId)
    private Map<Long, PlayerScore> playerScores = new HashMap<>();

    public void addPlayerResult(Long playerId, PlayerScore result) {
        playerScores.put(playerId, result);
    }

    public PlayerScore getPlayerResult(Long playerId) {
        return playerScores.get(playerId);
    }

    public double getPlayerScore(Long playerId) {
        PlayerScore result = playerScores.get(playerId);
        return result != null ? result.getTotalScore() : 0.0;
    }

    public boolean hasPlayerSubmittedResults(Long playerId) {
        return submittedById != null && submittedById.equals(playerId);
    }

    public boolean isSubmittedByOpponent(Long playerId) {
        return submittedById != null && !submittedById.equals(playerId);
    }

    public void calculateWinner() {
        if (playerScores.size() != 2) {
            return;
        }

        double maxScore = -1;
        Long winningPlayerId = null;

        for (Map.Entry<Long, PlayerScore> entry : playerScores.entrySet()) {
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