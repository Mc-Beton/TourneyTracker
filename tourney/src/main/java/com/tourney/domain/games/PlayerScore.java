package com.tourney.domain.games;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class PlayerScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "player_score_id") // Tworzy klucz obcy w tabeli RoundScore
    private List<RoundScore> roundScores = new ArrayList<>();

    public double getTotalScore() {
        return roundScores.stream()
                .mapToDouble(RoundScore::getTotalScore)
                .sum();
    }
}
