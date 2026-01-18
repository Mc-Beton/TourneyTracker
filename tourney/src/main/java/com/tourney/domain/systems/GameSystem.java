package com.tourney.domain.systems;

import com.tourney.domain.scores.ScoreType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gamesystems")
@Data
public class GameSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "default_round_number", nullable = false)
    private int defaultRoundNumber;

    @Column(name = "primary_score_enabled", nullable = false)
    private boolean primaryScoreEnabled = true;

    @Column(name = "secondary_score_enabled", nullable = false)
    private boolean secondaryScoreEnabled = true;

    @Column(name = "third_score_enabled", nullable = false)
    private boolean thirdScoreEnabled = false;

    @Column(name = "additional_score_enabled", nullable = false)
    private boolean additionalScoreEnabled = false;

    @Transient
    public List<ScoreType> getEnabledScoreTypes() {
        List<ScoreType> enabled = new ArrayList<>(4);

        if (primaryScoreEnabled) {
            enabled.add(ScoreType.MAIN_SCORE);
        }
        if (secondaryScoreEnabled) {
            enabled.add(ScoreType.SECONDARY_SCORE);
        }
        if (thirdScoreEnabled) {
            enabled.add(ScoreType.THIRD_SCORE);
        }
        if (additionalScoreEnabled) {
            enabled.add(ScoreType.ADDITIONAL_SCORE);
        }

        return enabled;
    }
}