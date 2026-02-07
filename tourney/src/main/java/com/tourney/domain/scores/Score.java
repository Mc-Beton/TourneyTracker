package com.tourney.domain.scores;

import com.tourney.domain.games.MatchRound;
import com.common.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(
        name = "scores",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_scores_round_side_type",
                        columnNames = {"match_round_id", "side", "score_type"}
                )
        },
        indexes = {
                @Index(name = "idx_scores_round", columnList = "match_round_id"),
                @Index(name = "idx_scores_entered_by", columnList = "entered_by_user_id")
        }
)
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_round_id", nullable = false)
    private MatchRound matchRound;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchSide side;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_type", nullable = false)
    private ScoreType scoreType;

    @Column(nullable = false)
    private Long score;

    @Column(name = "entered_by_user_id", nullable = false)
    private Long enteredByUserId;

    @Column(name = "entered_at", nullable = false)
    private LocalDateTime enteredAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (enteredAt == null) {
            enteredAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}