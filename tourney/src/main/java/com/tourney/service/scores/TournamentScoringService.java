package com.tourney.service.scores;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchRound;
import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentScoring;
import com.common.domain.User;
import com.tourney.repository.scores.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentScoringService {
    private final ScoreRepository scoreRepository;

    public void validateScoreSubmission(Tournament tournament, Match match, MatchRound round) {
        TournamentScoring scoring = tournament.getTournamentScoring();
        
        switch (scoring.getScoringSystem()) {
            case ROUND_BY_ROUND:
                // Można wprowadzać wyniki tylko dla aktualnej rundy
                if (!isCurrentRound(round)) {
                    throw new IllegalStateException("Można wprowadzać wyniki tylko dla aktualnej rundy");
                }
                break;
            case END_OF_MATCH:
                // Można wprowadzać wyniki tylko po zakończeniu meczu
                if (!isMatchFinished(match)) {
                    throw new IllegalStateException("Można wprowadzać wyniki tylko po zakończeniu meczu");
                }
                break;
        }
    }

    public void submitScores(MatchRound round, User user, Map<ScoreType, Long> scores) {
        com.tourney.domain.games.Match match = round.getMatch();
        if (!(match instanceof com.tourney.domain.games.TournamentMatch tournamentMatch)) {
            throw new IllegalArgumentException("Match is not a tournament match");
        }
        Tournament tournament = tournamentMatch.getTournamentRound().getTournament();
        TournamentScoring scoring = tournament.getTournamentScoring();

        validateScoreSubmission(tournament, round.getMatch(), round);

        // Sprawdź czy wszystkie wymagane typy punktów są podane
        if (scoring.isRequireAllScoreTypes() && 
            !scores.keySet().containsAll(scoring.getEnabledScoreTypes())) {
            throw new IllegalStateException("Należy podać wszystkie wymagane typy punktów");
        }

        // Walidacja zakresu punktów
        scores.forEach((type, value) -> {
            if (scoring.getMinScore() != null && value < scoring.getMinScore()) {
                throw new IllegalStateException("Punktacja poniżej dozwolonego minimum");
            }
            if (scoring.getMaxScore() != null && value > scoring.getMaxScore()) {
                throw new IllegalStateException("Punktacja powyżej dozwolonego maksimum");
            }
        });

        // Zapisz wyniki
        scores.forEach((type, value) -> {
            Score score = new Score();
            score.setMatchRound(round);
            score.setUser(user);
            score.setScoreType(type);
            score.setScore(value);
            scoreRepository.save(score);
        });
    }

    private boolean isCurrentRound(MatchRound round) {
        // Implementacja sprawdzania czy to aktualna runda
        return true; // TODO: zaimplementować logikę
    }

    private boolean isMatchFinished(Match match) {
        // Implementacja sprawdzania czy mecz jest zakończony
        return true; // TODO: zaimplementować logikę
    }

    private boolean isTournamentFinished(Tournament tournament) {
        // Implementacja sprawdzania czy turniej jest zakończony
        return true; // TODO: zaimplementować logikę
    }
}