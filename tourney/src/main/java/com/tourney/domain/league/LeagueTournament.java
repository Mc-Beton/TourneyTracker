package com.tourney.domain.league;

import com.common.domain.User;
import com.tourney.domain.tournament.Tournament;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// @Entity // Commented out - using direct Tournament.league relationship instead
// @Table(name = "league_tournaments") // Table dropped in V14 migration
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Deprecated // Replaced by Tournament.league relationship
public class LeagueTournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    // Status is now handled by the Tournament entity

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "submitted_at", insertable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
