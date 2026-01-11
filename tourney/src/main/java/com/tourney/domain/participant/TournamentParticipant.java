package com.tourney.domain.participant;

import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

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
}

