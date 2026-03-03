package com.tourney.domain.tournament;

import com.common.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TournamentChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "challenger_id", nullable = false)
    private User challenger;

    @ManyToOne
    @JoinColumn(name = "opponent_id", nullable = false)
    private User opponent;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    private LocalDateTime createdAt;
    
    // Flag to indicate if this challenge is still "relevant" 
    // (e.g. not cancelled by subsequent actions)
    // Could track if one of the users is locked in another challenge
    // But better done via queries.
}
