package com.tourney.service.match;

import com.common.domain.User;
import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchRound;
import com.tourney.domain.games.RoundStatus;
import com.tourney.domain.games.TournamentMatch;
import com.tourney.domain.scores.MatchSide;
import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.systems.GameSystem;
import com.tourney.dto.matches.MatchScoringDTO;
import com.tourney.dto.rounds.RoundScoresDTO;
import com.tourney.dto.scores.ScoreEntryDTO;
import com.tourney.dto.scores.SubmitScoreDTO;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.games.MatchRoundRepository;
import com.tourney.repository.scores.ScoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchScoringService {

    private final MatchRepository matchRepository;
    private final MatchRoundRepository matchRoundRepository;
    private final ScoreRepository scoreRepository;

    @Transactional(readOnly = true)
    public MatchScoringDTO getMatchScoring(Long matchId, Long currentUserId) {
        Match match = matchRepository.findMatchSummaryView(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        validateParticipant(match, currentUserId);

        List<MatchRound> rounds = matchRoundRepository.findByMatchId(matchId).stream()
                .sorted(Comparator.comparingInt(MatchRound::getRoundNumber))
                .toList();

        Map<Long, List<Score>> scoresByRoundId = scoreRepository.findAllByMatchIdWithRound(matchId).stream()
                .collect(Collectors.groupingBy(s -> s.getMatchRound().getId()));

        List<RoundScoresDTO> roundDtos = rounds.stream()
                .map(r -> toRoundScoresDTO(r, scoresByRoundId.getOrDefault(r.getId(), List.of())))
                .toList();

        int totalRounds = Math.max(0, rounds.size());
        int currentRound = rounds.stream()
                .filter(r -> r.getEndTime() == null) // nie zakończona
                .map(MatchRound::getRoundNumber)
                .min(Integer::compare) // najmniejszy numer
                .orElse(1);

        // Pobierz GameSystem - dla meczów turniejowych z tournament, dla zwykłych z match details
        GameSystem gs = null;
        if (match instanceof TournamentMatch tournamentMatch) {
            // Mecz turniejowy - pobierz system gry z turnieju
            gs = tournamentMatch.getTournamentRound().getTournament().getGameSystem();
        } else if (match.getDetails() != null) {
            // Zwykły mecz - pobierz system gry z detali meczu
            gs = match.getDetails().getGameSystem();
        }
        
        boolean primaryScoreEnabled = gs != null && gs.isPrimaryScoreEnabled();
        boolean secondaryScoreEnabled = gs != null && gs.isSecondaryScoreEnabled();
        boolean thirdScoreEnabled = gs != null && gs.isThirdScoreEnabled();
        boolean additionalScoreEnabled = gs != null && gs.isAdditionalScoreEnabled();

        return MatchScoringDTO.builder()
                .matchId(match.getId())
                .matchName(match.getDetails() != null ? match.getDetails().getMatchName() : null)
                .player1Name(match.getPlayer1() != null ? match.getPlayer1().getName() : null)
                .player2Name(resolvePlayer2Name(match))
                .currentRound(currentRound)
                .totalRounds(totalRounds)
                .startTime(match.getStartTime())
                .gameDurationMinutes(match.getGameDurationMinutes())
                .resultSubmissionDeadline(match.getResultSubmissionDeadline())
                .endTime(match.getGameEndTime())
                .primaryScoreEnabled(primaryScoreEnabled)
                .secondaryScoreEnabled(secondaryScoreEnabled)
                .thirdScoreEnabled(thirdScoreEnabled)
                .additionalScoreEnabled(additionalScoreEnabled)
                .rounds(roundDtos)
                .build();
    }

    @Transactional
    public MatchScoringDTO submitRoundScores(Long matchId, SubmitScoreDTO submitScoreDTO, Long currentUserId) {
        Match match = matchRepository.findMatchSummaryView(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        validateParticipant(match, currentUserId);

        if (submitScoreDTO == null) {
            throw new IllegalArgumentException("Brak danych do zapisu wyników.");
        }
        if (submitScoreDTO.getRoundNumber() <= 0) {
            throw new IllegalArgumentException("roundNumber musi być > 0");
        }
        if (submitScoreDTO.getScores() == null || submitScoreDTO.getScores().isEmpty()) {
            // partial może być puste, ale zwykle to błąd klienta
            throw new IllegalArgumentException("scores nie może być puste");
        }

        MatchRound round = matchRoundRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getRoundNumber() == submitScoreDTO.getRoundNumber())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));

        LocalDateTime now = LocalDateTime.now();

        for (ScoreEntryDTO e : submitScoreDTO.getScores()) {
            if (e == null) continue;
            if (e.getSide() == null) {
                throw new IllegalArgumentException("side jest wymagane");
            }
            if (e.getScoreType() == null) {
                throw new IllegalArgumentException("scoreType jest wymagane");
            }

            Score score = scoreRepository
                    .findByMatchRoundIdAndSideAndScoreType(round.getId(), e.getSide(), e.getScoreType())
                    .orElseGet(() -> {
                        Score s = new Score();
                        s.setMatchRound(round);
                        s.setSide(e.getSide());
                        s.setScoreType(e.getScoreType());
                        return s;
                    });

            score.setUser(mapSideToUser(match, e.getSide())); // null dla hotseat PLAYER2
            score.setScore(e.getScore() != null ? e.getScore() : 0L);

            score.setEnteredByUserId(currentUserId);
            score.setEnteredAt(now);

            scoreRepository.save(score);
        }

        return getMatchScoring(matchId, currentUserId);
    }

    @Transactional
    public MatchScoringDTO startRound(Long matchId, Long currentUserId) {
        Match match = matchRepository.findMatchSummaryView(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        validateParticipant(match, currentUserId);

        List<MatchRound> rounds = matchRoundRepository.findByMatchId(matchId);

        // Znajdź pierwszą rundę, która jeszcze nie została rozpoczęta
        MatchRound roundToStart = rounds.stream()
                .filter(r -> r.getStartTime() == null)
                .min(Comparator.comparingInt(MatchRound::getRoundNumber))
                .orElseThrow(() -> new IllegalStateException("Wszystkie rundy już rozpoczęte"));

        // Rozpocznij tę rundę
        roundToStart.setStartTime(LocalDateTime.now());
        matchRoundRepository.save(roundToStart);

        return getMatchScoring(matchId, currentUserId);
    }

    @Transactional
    public MatchScoringDTO endRound(Long matchId, int roundNumber, Long currentUserId) {
        Match match = matchRepository.findMatchSummaryView(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        validateParticipant(match, currentUserId);

        if (roundNumber <= 0) {
            throw new IllegalArgumentException("roundNumber musi być > 0");
        }

        MatchRound round = matchRoundRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));

        if (round.getEndTime() == null) {
            round.setEndTime(LocalDateTime.now());
            matchRoundRepository.save(round);
        }

        return getMatchScoring(matchId, currentUserId);
    }

    private RoundScoresDTO toRoundScoresDTO(MatchRound round, List<Score> scores) {
        // indeks: side -> type -> value
        EnumMap<MatchSide, EnumMap<ScoreType, Long>> bySide = new EnumMap<>(MatchSide.class);
        for (Score s : scores) {
            bySide.computeIfAbsent(s.getSide(), k -> new EnumMap<>(ScoreType.class))
                    .merge(s.getScoreType(), s.getScore() != null ? s.getScore() : 0L, Long::sum);
        }

        Long p1Main = get(bySide, MatchSide.PLAYER1, ScoreType.MAIN_SCORE);
        Long p1Sec = get(bySide, MatchSide.PLAYER1, ScoreType.SECONDARY_SCORE);
        Long p2Main = get(bySide, MatchSide.PLAYER2, ScoreType.MAIN_SCORE);
        Long p2Sec = get(bySide, MatchSide.PLAYER2, ScoreType.SECONDARY_SCORE);

        RoundStatus status = inferStatus(round);

        return RoundScoresDTO.builder()
                .roundNumber(round.getRoundNumber())
                .player1MainScore(p1Main)
                .player1SecondaryScore(p1Sec)
                .player2MainScore(p2Main)
                .player2SecondaryScore(p2Sec)
                .startTime(round.getStartTime())
                .endTime(round.getEndTime())
                .status(status)
                .build();
    }

    private RoundStatus inferStatus(MatchRound r) {
        // Jeśli masz inne zasady w domenie, podmień.
        if (r.getStartTime() == null) return RoundStatus.NOT_STARTED;
        if (r.getEndTime() == null) return RoundStatus.IN_PROGRESS;
        return RoundStatus.FINISHED;
    }

    private Long get(EnumMap<MatchSide, EnumMap<ScoreType, Long>> bySide, MatchSide side, ScoreType type) {
        return bySide.getOrDefault(side, new EnumMap<>(ScoreType.class)).getOrDefault(type, 0L);
    }

    private void validateParticipant(Match match, Long currentUserId) {
        boolean isP1 = match.getPlayer1() != null && Objects.equals(match.getPlayer1().getId(), currentUserId);
        boolean isP2 = match.getPlayer2() != null && Objects.equals(match.getPlayer2().getId(), currentUserId);
        if (!isP1 && !isP2) {
            throw new IllegalArgumentException("Brak dostępu do tego meczu.");
        }
    }

    private User mapSideToUser(Match match, MatchSide side) {
        return switch (side) {
            case PLAYER1 -> match.getPlayer1();
            case PLAYER2 -> match.getPlayer2(); // null dla hotseat
        };
    }

    private String resolvePlayer2Name(Match match) {
        if (match.getPlayer2() != null) {
            return match.getPlayer2().getName();
        }
        String guest = match.getDetails() != null ? match.getDetails().getGuestPlayer2Name() : null;
        return StringUtils.hasText(guest) ? guest.trim() : null;
    }
}