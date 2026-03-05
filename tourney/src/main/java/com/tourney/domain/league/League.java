package com.tourney.domain.league;

import com.common.domain.User;
import com.tourney.domain.systems.GameSystem;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leagues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_system_id", nullable = false)
    private GameSystem gameSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "auto_accept_games", nullable = false)
    @Builder.Default
    private boolean autoAcceptGames = false;

    @Column(name = "auto_accept_tournaments", nullable = false)
    @Builder.Default
    private boolean autoAcceptTournaments = false;

    // Scoring System
    @Column(name = "points_win", nullable = false)
    @Builder.Default
    private int pointsWin = 3;

    @Column(name = "points_draw", nullable = false)
    @Builder.Default
    private int pointsDraw = 1;

    @Column(name = "points_loss", nullable = false)
    @Builder.Default
    private int pointsLoss = 0;

    @Column(name = "points_participation", nullable = false)
    @Builder.Default
    private int pointsParticipation = 1;

    @Column(name = "points_per_participant", nullable = false)
    @Builder.Default
    private int pointsPerParticipant = 1;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
