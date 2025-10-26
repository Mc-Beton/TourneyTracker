package com.tourney.util;

import com.tourney.domain.games.Match;
import com.tourney.domain.scores.TieBreaker;
import com.tourney.domain.tournament.Tournament;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class TieBreakerCalculator {
    
    public List<TieBreaker> calculateTieBreakers(Tournament tournament, Long playerId) {
        return List.of(
            calculateMatchWins(tournament, playerId),
            calculateMatchWinPercentage(tournament, playerId),
            calculateGameWinPercentage(tournament, playerId),
            calculateOpponentsMatchWinPercentage(tournament, playerId),
            calculateTotalScore(tournament, playerId)
        );
    }

    private TieBreaker calculateMatchWins(Tournament tournament, Long playerId) {
        long wins = tournament.getRounds().stream()
                .flatMap(round -> round.getMatches().stream())
                .filter(match -> match.isCompleted() && isPlayerWinner(match, playerId))
                .count();

        return TieBreaker.builder()
                .type(TieBreaker.TieBreakerType.MATCH_WINS)
                .value((double) wins)
                .priority(1)
                .build();
    }

    private TieBreaker calculateMatchWinPercentage(Tournament tournament, Long playerId) {
        long totalMatches = tournament.getRounds().stream()
                .flatMap(round -> round.getMatches().stream())
                .filter(Match::isCompleted)
                .filter(match -> isPlayerInMatch(match, playerId))
                .count();

        if (totalMatches == 0) {
            return TieBreaker.builder()
                    .type(TieBreaker.TieBreakerType.MATCH_WIN_PERCENTAGE)
                    .value(0.0)
                    .priority(2)
                    .build();
        }

        long wins = tournament.getRounds().stream()
                .flatMap(round -> round.getMatches().stream())
                .filter(match -> match.isCompleted() && isPlayerWinner(match, playerId))
                .count();

        return TieBreaker.builder()
                .type(TieBreaker.TieBreakerType.MATCH_WIN_PERCENTAGE)
                .value((double) wins / totalMatches * 100)
                .priority(2)
                .build();
    }

    private TieBreaker calculateGameWinPercentage(Tournament tournament, Long playerId) {
        // TODO: Implementacja dla gier wewnątrz meczu
        return TieBreaker.builder()
                .type(TieBreaker.TieBreakerType.GAME_WIN_PERCENTAGE)
                .value(0.0)
                .priority(3)
                .build();
    }

    private TieBreaker calculateOpponentsMatchWinPercentage(Tournament tournament, Long playerId) {
        // TODO: Implementacja dla średniego wyniku przeciwników
        return TieBreaker.builder()
                .type(TieBreaker.TieBreakerType.OPPONENTS_MATCH_WIN_PERCENTAGE)
                .value(0.0)
                .priority(4)
                .build();
    }

    private TieBreaker calculateTotalScore(Tournament tournament, Long playerId) {
        double totalScore = tournament.getRounds().stream()
                .flatMap(round -> round.getMatches().stream())
                .filter(match -> match.isCompleted() && isPlayerInMatch(match, playerId))
                .mapToDouble(match -> getPlayerScore(match, playerId))
                .sum();

        return TieBreaker.builder()
                .type(TieBreaker.TieBreakerType.TOTAL_SCORE)
                .value(totalScore)
                .priority(5)
                .build();
    }

    private boolean isPlayerInMatch(Match match, Long playerId) {
        return match.getPlayer1().getId().equals(playerId) || 
               match.getPlayer2().getId().equals(playerId);
    }

private double getPlayerScore(Match match, Long playerId) {
    if (match.getMatchResult() == null) {
        return 0.0;
    }
    return match.getMatchResult().getPlayerScore(playerId);
}

private boolean isPlayerWinner(Match match, Long playerId) {
    if (!match.isCompleted()) {
        return false;
    }
    return match.getMatchResult() != null && 
           match.getMatchResult().getWinnerId() != null && 
           match.getMatchResult().getWinnerId().equals(playerId);
}
}