package com.tourney.service.scores;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchRound;
import com.tourney.domain.scores.MatchSide;
import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.user.User;
import com.tourney.dto.scores.RoundScoreSubmissionDTO;
import com.tourney.repository.games.MatchRoundRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final MatchRoundRepository matchRoundRepository;
    private final UserRepository userRepository;
    private final TournamentScoringService tournamentScoringService;

    public void submitRoundScores(RoundScoreSubmissionDTO submissionDTO, Long currentUserId) {
        MatchRound round = matchRoundRepository.findById(submissionDTO.getMatchRoundId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono rundy o ID: " + submissionDTO.getMatchRoundId()));

        Match match = round.getMatch();

        // Tylko uczestnik meczu może edytować (ale może edytować obie strony)
        boolean isPlayer1 = match.getPlayer1() != null && currentUserId.equals(match.getPlayer1().getId());
        boolean isPlayer2 = match.getPlayer2() != null && currentUserId.equals(match.getPlayer2().getId());
        if (!isPlayer1 && !isPlayer2) {
            throw new RuntimeException("Użytkownik nie jest uczestnikiem tego meczu");
        }

        // Walidacje turniejowe (jeśli dotyczy)
        if (match.getTournamentRound() != null) {
            Tournament tournament = match.getTournamentRound().getTournament();
            tournamentScoringService.validateScoreSubmission(tournament, match, round);
            submissionDTO.getScores().forEach((scoreType, value) -> validateScoreType(tournament, scoreType));
        }

        User mappedUser = mapSideToUser(match, submissionDTO.getSide()); // null dla hot-seat PLAYER2
        LocalDateTime now = LocalDateTime.now();

        // 1) Wczytaj istniejące wyniki dla tej rundy i tej strony
        List<Score> existing = scoreRepository.findByMatchRoundIdAndSide(round.getId(), submissionDTO.getSide());

        Map<ScoreType, Score> byType = new EnumMap<>(ScoreType.class);
        for (Score s : existing) {
            byType.put(s.getScoreType(), s);
        }

        // 2) Upsert dla typów z DTO
        for (Map.Entry<ScoreType, Long> entry : submissionDTO.getScores().entrySet()) {
            ScoreType type = entry.getKey();
            Long value = entry.getValue();

            Score score = byType.get(type);
            if (score == null) {
                score = new Score();
                score.setMatchRound(round);
                score.setSide(submissionDTO.getSide());
                score.setScoreType(type);
            }

            score.setUser(mappedUser);
            score.setScore(value != null ? value : 0L);

            // AUDYT: kto i kiedy nadpisał
            score.setEnteredByUserId(currentUserId);
            score.setEnteredAt(now);

            scoreRepository.save(score);
        }

        // 3) (Opcjonalnie, ale spójne) usuń typy, których nie ma w DTO
        // Dzięki temu stan w DB. = dokładnie to, co klient wysłał.
        for (Score old : existing) {
            if (!submissionDTO.getScores().containsKey(old.getScoreType())) {
                scoreRepository.delete(old);
            }
        }
    }

    private User mapSideToUser(Match match, MatchSide side) {
        return switch (side) {
            case PLAYER1 -> match.getPlayer1();
            case PLAYER2 -> match.getPlayer2();
        };
    }

    private void validateScoreType(Tournament tournament, ScoreType scoreType) {
        if (!tournament.getTournamentScoring().getEnabledScoreTypes().contains(scoreType)) {
            throw new RuntimeException("Niedozwolony typ punktacji: " + scoreType);
        }
    }
}