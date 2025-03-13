package com.tourney.domain.games;

import com.tourney.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private int gameDurationMinutes;
    private LocalDateTime resultSubmissionDeadline;

    @ManyToOne
    @JoinColumn(name = "round_id")
    private Round round;

    @ManyToOne
    @JoinColumn(name = "player1_id")
    private User player1;

    @ManyToOne
    @JoinColumn(name = "player2_id")
    private User player2;

    @Embedded
    private MatchResult matchResult;
}
