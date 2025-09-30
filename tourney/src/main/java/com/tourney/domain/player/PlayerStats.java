package com.tourney.domain.player;

import com.tourney.domain.user.User;
import lombok.Getter;

@Getter
public class PlayerStats {
    private final User user;
    private int wins;
    private long totalPoints;
    private double buchholzScore; // dodajemy wsparcie dla systemu Buchholza na przyszłość
    private int gamesPlayed;

    public PlayerStats(User user) {
        this.user = user;
        this.wins = 0;
        this.totalPoints = 0;
        this.buchholzScore = 0.0;
        this.gamesPlayed = 0;
    }

    public void incrementWins() {
        wins++;
    }

    public void addPoints(Long points) {
        totalPoints += points != null ? points : 0;
    }

    public void incrementGamesPlayed() {
        gamesPlayed++;
    }

    public void updateBuchholzScore(double score) {
        this.buchholzScore = score;
    }

    public double getWinRate() {
        return gamesPlayed == 0 ? 0.0 : (double) wins / gamesPlayed;
    }

    public double getAveragePoints() {
        return gamesPlayed == 0 ? 0.0 : (double) totalPoints / gamesPlayed;
    }
}