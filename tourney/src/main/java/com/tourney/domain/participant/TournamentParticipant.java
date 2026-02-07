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

    @PrePersist
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
    }
}

