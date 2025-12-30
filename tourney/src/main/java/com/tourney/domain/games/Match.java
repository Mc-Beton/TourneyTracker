package com.tourney.domain.games;

import com.tourney.domain.tournament.TournamentRound;
import com.tourney.domain.user.User;
import com.tourney.exception.MatchOperationException;
import com.tourney.exception.domain.MatchErrorCode;
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
    private Integer tableNumber;
    private LocalDateTime gameEndTime;

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

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "match_result_id")
    private MatchResult matchResult;

    private boolean player1Ready;
    private boolean player2Ready;
    private boolean player1Confirmed;
    private boolean player2Confirmed;
    private boolean resultsConfirmed;
    private boolean isCompleted;

    public boolean isPlayerReady(Long playerId) {
        if (player1.getId().equals(playerId)) {
            return player1Ready;
        }
        if (player2.getId().equals(playerId)) {
            return player2Ready;
        }
        throw new MatchOperationException(MatchErrorCode.PLAYER_NOT_IN_MATCH);
    }

    public boolean isOpponentReady(Long playerId) {
        if (!player1.getId().equals(playerId)) {
            return player1Ready;
        }
        if (!player2.getId().equals(playerId)) {
            return player2Ready;
        }
        throw new MatchOperationException(MatchErrorCode.PLAYER_NOT_IN_MATCH);
    }

    public boolean hasPlayerSubmittedResults(Long playerId) {
        return matchResult != null && matchResult.hasPlayerSubmittedResults(playerId);
    }

    public boolean needsConfirmationFrom(Long playerId) {
        if (matchResult == null) {
            return false;
        }
        // Jeśli przeciwnik wprowadził wyniki, a ten gracz jeszcze nie potwierdził
        return matchResult.isSubmittedByOpponent(playerId) &&
                !isPlayerConfirmed(playerId);
    }

    public boolean isPlayerConfirmed(Long playerId) {
        if (player1.getId().equals(playerId)) {
            return player1Confirmed;
        }
        if (player2.getId().equals(playerId)) {
            return player2Confirmed;
        }
        throw new MatchOperationException(MatchErrorCode.PLAYER_NOT_IN_MATCH);
    }

    public void setPlayerReady(Long playerId) {
        if (player1.getId().equals(playerId)) {
            player1Ready = true;
        }
        if (player2.getId().equals(playerId)) {
            player2Ready = true;
        }
    }

    public boolean areBothPlayersReady() {
        return player1Ready && player2Ready;
    }

    public void confirmResults(Long playerId) {
        if (player1.getId().equals(playerId)) {
            player1Confirmed = true;
        }
        if (player2.getId().equals(playerId)) {
            player2Confirmed = true;
        }
    }

    public boolean areBothPlayersConfirmed() {
        return player1Confirmed && player2Confirmed;
    }

    public long getOpponentId(Long playerId) {
        if (player1.getId().equals(playerId)) {
            return player2.getId();
        }
        if (player2.getId().equals(playerId)) {
            return player1.getId();
        }
        throw new MatchOperationException(MatchErrorCode.PLAYER_NOT_IN_MATCH);
    }
}