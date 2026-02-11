package com.tourney.domain.participant;

import com.tourney.domain.systems.Army;
import com.tourney.domain.systems.ArmyFaction;
import com.tourney.domain.tournament.Tournament;
import com.common.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_participants")
@IdClass(TournamentParticipantId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TournamentParticipant {

    @Id
    @Column(name = "tournament_id")
    private Long tournamentId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", insertable = false, updatable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    private boolean confirmed = false;

    // Payment status
    @Column(name = "is_paid", nullable = false)
    private boolean isPaid = false;

    // Army list fields
    @Column(name = "army_list_submitted", nullable = false)
    private boolean armyListSubmitted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "army_list_status")
    private ArmyListStatus armyListStatus = ArmyListStatus.NOT_SUBMITTED;

    @Column(name = "army_list_content", columnDefinition = "TEXT")
    private String armyListContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "army_faction_id")
    private ArmyFaction armyFaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "army_id")
    private Army army;

    @Column(name = "army_list_submitted_at")
    private LocalDateTime armyListSubmittedAt;

    @Column(name = "army_list_reviewed_at")
    private LocalDateTime armyListReviewedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    // Tournament stats - updated after each match
    @Column(name = "tournament_points", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int tournamentPoints = 0;

    @Column(name = "score_points", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private long scorePoints = 0L;

    @Column(name = "matches_played", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int matchesPlayed = 0;

    @Column(name = "wins", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int wins = 0;

    @Column(name = "draws", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int draws = 0;

    @Column(name = "losses", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int losses = 0;

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
    }

    /**
     * Dodaje punkty po zakończonym meczu
     */
    public void addMatchResult(int tournamentPoints, long scorePoints, MatchResult result) {
        this.tournamentPoints += tournamentPoints;
        this.scorePoints += scorePoints;
        this.matchesPlayed++;
        
        switch (result) {
            case WIN -> this.wins++;
            case DRAW -> this.draws++;
            case LOSS -> this.losses++;
        }
    }

    /**
     * Resetuje statystyki turnieju
     */
    public void resetStats() {
        this.tournamentPoints = 0;
        this.scorePoints = 0L;
        this.matchesPlayed = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
    }

    public enum MatchResult {
        WIN, DRAW, LOSS
    }
}

