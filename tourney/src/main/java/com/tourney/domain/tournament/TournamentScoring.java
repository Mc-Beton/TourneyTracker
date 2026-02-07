package com.tourney.domain.tournament;

import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.scores.ScoringSystem;
import com.tourney.domain.scores.TournamentPointsSystem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
public class TournamentScoring {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    // === SYSTEM MAŁYCH PUNKTÓW (Score Points) ===
    @Enumerated(EnumType.STRING)
    private ScoringSystem scoringSystem;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tournament_score_types")
    private Set<ScoreType> enabledScoreTypes;

    private boolean requireAllScoreTypes;
    
    private Integer minScore;
    
    private Integer maxScore;

    // === SYSTEM DUŻYCH PUNKTÓW (Tournament Points) ===
    @Enumerated(EnumType.STRING)
    @Column(name = "tournament_points_system")
    private TournamentPointsSystem tournamentPointsSystem;

    // Dla systemu FIXED
    @Column(name = "points_for_win")
    private Integer pointsForWin;

    @Column(name = "points_for_draw")
    private Integer pointsForDraw;

    @Column(name = "points_for_loss")
    private Integer pointsForLoss;
}