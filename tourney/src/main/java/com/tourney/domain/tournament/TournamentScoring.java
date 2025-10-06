package com.tourney.domain.tournament;

import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.scores.ScoringSystem;
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

    @Enumerated(EnumType.STRING)
    private ScoringSystem scoringSystem;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tournament_score_types")
    private Set<ScoreType> enabledScoreTypes;

    private boolean requireAllScoreTypes;
    
    private Integer minScore;
    
    private Integer maxScore;
}