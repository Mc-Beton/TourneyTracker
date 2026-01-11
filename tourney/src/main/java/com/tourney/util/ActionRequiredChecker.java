package com.tourney.util;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.dto.tournament.TournamentStatus;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ActionRequiredChecker {

    public static boolean isActionRequired(Tournament tournament, Long userId) {
        if (tournament == null || userId == null) {
            return false;
        }

        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            return false;
        }

        boolean isParticipant = tournament.getParticipantLinks().stream()
                .anyMatch(link -> link.getUserId() != null
                        && link.getUserId().equals(userId)
                        && link.isConfirmed());

        if (!isParticipant) {
            return false;
        }


        // Pobierz aktualną rundę z listy rund
        TournamentRound currentRound = tournament.getRounds().stream()
                .filter(round -> round.getRoundNumber() == tournament.getCurrentRound())
                .findFirst()
                .orElse(null);

        if (currentRound == null) {
            return false;
        }

        return currentRound.getMatches().stream()
                .anyMatch(match -> {
                    // Sprawdź czy mecz dotyczy danego użytkownika
                    boolean isUserInvolved = match.getPlayer1().getId().equals(userId) || 
                                          match.getPlayer2().getId().equals(userId);
                    
                    if (!isUserInvolved) {
                        return false;
                    }

                    // Sprawdź czy jest potrzebna jakaś akcja
                    switch (match.getStatus()) {
                        case SCHEDULED:
                            return !match.isPlayerReady(userId); // Gracz musi potwierdzić gotowość
                        case IN_PROGRESS:
                            return !match.hasPlayerSubmittedResults(userId); // Gracz musi wprowadzić wyniki
                        case WAITING_CONFIRMATION:
                            return match.needsConfirmationFrom(userId); // Gracz musi potwierdzić wyniki
                        default:
                            return false;
                    }
                });
    }

    public boolean needsConfirmation(Long matchId, Long userId) {
        // TODO
        return true;
    }

    public boolean needsScoreSubmission(Long matchId, Long userId) {
        // TODO
        return true;
    }

    public boolean needsReadyConfirmation(Long matchId, Long userId) {
        // TODO
        return true;
    }
}