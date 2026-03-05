package com.tourney.domain.league;

import com.common.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "league_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeagueMemberStatus status = LeagueMemberStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private int points = 0;

    @Column(name = "matches_played", nullable = false)
    @Builder.Default
    private int matchesPlayed = 0;

    @Column(name = "wins", nullable = false)
    @Builder.Default
    private int wins = 0;

    @Column(name = "draws", nullable = false)
    @Builder.Default
    private int draws = 0;

    @Column(name = "losses", nullable = false)
    @Builder.Default
    private int losses = 0;

    @Column(name = "tournaments_played", nullable = false)
    @Builder.Default
    private int tournamentsPlayed = 0;

    @Column(name = "tournament_wins", nullable = false)
    @Builder.Default
    private int tournamentWins = 0;

    @Column(name = "points_scored", nullable = false)
    @Builder.Default
    private int pointsScored = 0;  // Total points scored in games

    @Column(name = "joined_at", insertable = false, updatable = false)
    private LocalDateTime joinedAt;
}
