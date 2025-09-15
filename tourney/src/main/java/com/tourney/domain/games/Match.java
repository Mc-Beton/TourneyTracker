package com.tourney.domain.games;

import com.tourney.domain.tournament.TournamentRound;
import com.tourney.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private int gameDurationMinutes;
    private LocalDateTime resultSubmissionDeadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private TournamentRound tournamentRound;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id")
    private User player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id")
    private User player2;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<MatchRound> rounds = new ArrayList<>();

    @Embedded
    private MatchResult matchResult;
}