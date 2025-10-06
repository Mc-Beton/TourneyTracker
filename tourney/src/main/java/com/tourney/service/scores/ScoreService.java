package com.tourney.service.scores;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchRound;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.user.User;
import com.tourney.dto.scores.RoundScoreSubmissionDTO;
import com.tourney.repository.games.MatchRoundRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScoreService {
    private final ScoreRepository scoreRepository;
    private final MatchRoundRepository matchRoundRepository;
    private final UserRepository userRepository;
    private final TournamentScoringService tournamentScoringService;

    public void submitRoundScores(RoundScoreSubmissionDTO submissionDTO) {
        MatchRound round = matchRoundRepository.findById(submissionDTO.getMatchRoundId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono rundy o ID: " + submissionDTO.getMatchRoundId()));

        User user = userRepository.findById(submissionDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika o ID: " + submissionDTO.getUserId()));

        Match match = round.getMatch();
        Tournament tournament = match.getTournamentRound().getTournament();

        // Sprawdź czy użytkownik jest uczestnikiem meczu
        if (!isMatchParticipant(match, user)) {
            throw new RuntimeException("Użytkownik nie jest uczestnikiem tego meczu");
        }

        // Walidacja możliwości wprowadzenia wyników
        tournamentScoringService.validateScoreSubmission(tournament, match, round);

        // Usuń poprzednie wyniki dla tej rundy i użytkownika (jeśli istnieją)
        scoreRepository.deleteByMatchRoundAndUser(round, user);

        // Zapisz nowe wyniki
        submissionDTO.getScores().forEach((scoreType, value) -> {
            validateScoreType(tournament, scoreType);

            Score score = new Score();
            score.setMatchRound(round);
            score.setUser(user);
            score.setScoreType(scoreType);
            score.setScore(value);

            scoreRepository.save(score);
        });
    }

    private boolean isMatchParticipant(Match match, User user) {
        return user.getId().equals(match.getPlayer1().getId()) ||
                user.getId().equals(match.getPlayer2().getId());
    }

    private void validateScoreType(Tournament tournament, ScoreType scoreType) {
        if (!tournament.getTournamentScoring().getEnabledScoreTypes().contains(scoreType)) {
            throw new RuntimeException("Niedozwolony typ punktacji: " + scoreType);
        }
    }
}

