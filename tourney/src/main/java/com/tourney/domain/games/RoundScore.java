package com.tourney.domain.games;

import com.tourney.domain.scores.ScoreType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Entity // Zmieniono z @Embeddable na @Entity
@Getter
@Setter
public class RoundScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "round_score_values")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "score_value")
    private Map<ScoreType, Double> scores = new HashMap<>();

    public double getTotalScore() {
        return scores.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
