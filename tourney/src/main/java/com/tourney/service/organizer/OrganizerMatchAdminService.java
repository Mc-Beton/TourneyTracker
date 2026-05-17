package com.tourney.service.organizer;

import com.common.domain.User;
import com.tourney.domain.games.*;
import com.tourney.domain.scores.MatchSide;
import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.matches.MatchScoringDTO;
import com.tourney.dto.rounds.RoundScoresDTO;
import com.tourney.dto.scores.AdminBulkEditScoresDTO;
import com.tourney.dto.scores.ScoreEntryDTO;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.games.MatchRoundRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.service.tournament.ParticipantStatsUpdateService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizerMatchAdminService {

    private final MatchRepository matchRepository;
    private final MatchRoundRepository matchRoundRepository;
    private final ScoreRepository scoreRepository;
    private final ParticipantStatsUpdateService participantStatsUpdateService;

    @Transactional(readOnly = true)
    public MatchScoringDTO getScoringForOrganizer(Long matchId, Long organizerId) {
        Match match = matchRepository.findMatchSummaryView(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        Tournament tournament = validateOrganizerAccessAndState(match, organizerId, false);

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
                .filter(r -> r.getEndTime() == null) // not finished
                .map(MatchRound::getRoundNumber)
                .min(Integer::compare)
                .orElse(1);

        GameSystem gs = null;
        if (match instanceof TournamentMatch tm) {
            gs = tm.getTournamentRound().getTournament().getGameSystem();
        } else if (match.getDetails() != null) {
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
                .startTime(toUtc(match.getStartTime()))
                .gameDurationMinutes(match.getGameDurationMinutes())
                .resultSubmissionDeadline(toUtc(match.getResultSubmissionDeadline()))
                .endTime(toUtc(match.getGameEndTime()))
                .primaryScoreEnabled(primaryScoreEnabled)
                .secondaryScoreEnabled(secondaryScoreEnabled)
                .thirdScoreEnabled(thirdScoreEnabled)
                .additionalScoreEnabled(additionalScoreEnabled)
                .rounds(roundDtos)
                .build();
    }

    @Transactional
    public MatchScoringDTO updateScoresBulk(Long matchId, Long organizerId, AdminBulkEditScoresDTO payload) {
        if (payload == null || payload.getRounds() == null || payload.getRounds().isEmpty()) {
            throw new IllegalArgumentException("Brak danych do zapisu wyników.");
        }

        Match match = matchRepository.findMatchSummaryView(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        Tournament tournament = validateOrganizerAccessAndState(match, organizerId, true);

        // Map roundNumber -> MatchRound
        Map<Integer, MatchRound> roundsByNumber = matchRoundRepository.findByMatchId(matchId).stream()
                .collect(Collectors.toMap(MatchRound::getRoundNumber, r -> r));

        LocalDateTime now = LocalDateTime.now();

        payload.getRounds().forEach(roundEdit -> {
            MatchRound round = roundsByNumber.get(roundEdit.getRoundNumber());
            if (round == null) {
                throw new EntityNotFoundException("Round not found: " + roundEdit.getRoundNumber());
            }
            List<ScoreEntryDTO> entries = roundEdit.getScores();
            if (entries == null) return;

            for (ScoreEntryDTO e : entries) {
                if (e == null || e.getSide() == null || e.getScoreType() == null) continue;
                Score score = scoreRepository
                        .findByMatchRoundIdAndSideAndScoreType(round.getId(), e.getSide(), e.getScoreType())
                        .orElseGet(() -> {
                            Score s = new Score();
                            s.setMatchRound(round);
                            s.setSide(e.getSide());
                            s.setScoreType(e.getScoreType());
                            return s;
                        });

                score.setUser(mapSideToUser(match, e.getSide()));
                Integer val = e.getScore() != null ? e.getScore() : 0;
                score.setScore(val.longValue());
                score.setEnteredByUserId(organizerId);
                score.setEnteredAt(now);
                scoreRepository.save(score);
            }
        });

        // Recompute MatchResult from scores
        MatchResult newResult = createMatchResultFromScores(match, organizerId);
        match.setMatchResult(newResult);
        // status/gameEndTime pozostają bez zmian (mecz już zakończony)
        matchRepository.save(match);

        // Full tournament stats recomputation to ensure consistency
        participantStatsUpdateService.recalculateAllStats(tournament);

        // Return updated scoring for organizer
        return getScoringForOrganizer(matchId, organizerId);
    }

    private Tournament validateOrganizerAccessAndState(Match match, Long organizerId, boolean enforceCompleted) {
        if (!(match instanceof TournamentMatch tm)) {
            throw new IllegalArgumentException("Operacja dostępna tylko dla meczów turniejowych");
        }
        if (tm.getTournamentRound() == null || tm.getTournamentRound().getTournament() == null) {
            throw new IllegalStateException("Brak powiązania meczu z turniejem");
        }
        Tournament tournament = tm.getTournamentRound().getTournament();
        User organizer = tournament.getOrganizer();
        if (organizer == null || !Objects.equals(organizer.getId(), organizerId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Brak uprawnień do edycji wyników tego turnieju");
        }
        // Liga: zablokuj edycję po przydzieleniu punktów
        if (Boolean.TRUE.equals(tournament.getLeaguePointsAssigned())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Wyniki turnieju zostały już zatwierdzone w lidze — edycja zablokowana");
        }
        if (enforceCompleted && match.getStatus() != MatchStatus.COMPLETED) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT,
                    "Mecz nie jest zakończony — edycji dokonuj w widoku rozgrywki");
        }
        return tournament;
    }

    private String resolvePlayer2Name(Match match) {
        if (match.getPlayer2() != null) {
            return match.getPlayer2().getName();
        }
        String guest = match.getDetails() != null ? match.getDetails().getGuestPlayer2Name() : null;
        return (guest != null && !guest.isBlank()) ? guest.trim() : null;
    }

    private User mapSideToUser(Match match, MatchSide side) {
        return switch (side) {
            case PLAYER1 -> match.getPlayer1();
            case PLAYER2 -> match.getPlayer2();
        };
    }

    private RoundScoresDTO toRoundScoresDTO(MatchRound round, List<Score> scores) {
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

    private Long get(EnumMap<MatchSide, EnumMap<ScoreType, Long>> bySide, MatchSide side, ScoreType type) {
        return bySide.getOrDefault(side, new EnumMap<>(ScoreType.class)).getOrDefault(type, 0L);
    }

    private RoundStatus inferStatus(MatchRound r) {
        if (r.getStartTime() == null) return RoundStatus.NOT_STARTED;
        if (r.getEndTime() == null) return RoundStatus.IN_PROGRESS;
        return RoundStatus.FINISHED;
    }

    private OffsetDateTime toUtc(LocalDateTime local) {
        if (local == null) return null;
        return local.atOffset(ZoneOffset.UTC);
    }

    private MatchResult createMatchResultFromScores(Match match, Long submitterId) {
        List<Score> scores = scoreRepository.findAllByMatchIdWithRound(match.getId());

        MatchResult result = new MatchResult();
        result.setSubmissionTime(LocalDateTime.now());
        result.setSubmittedById(submitterId);

        Map<Long, Map<Integer, RoundScore>> playerRoundScores = new HashMap<>();

        for (Score s : scores) {
            Long playerId = null;
            if (s.getSide() == MatchSide.PLAYER1 && match.getPlayer1() != null) {
                playerId = match.getPlayer1().getId();
            } else if (s.getSide() == MatchSide.PLAYER2 && match.getPlayer2() != null) {
                playerId = match.getPlayer2().getId();
            }
            if (playerId == null) continue;

            playerRoundScores.computeIfAbsent(playerId, k -> new HashMap<>());
            Map<Integer, RoundScore> userRounds = playerRoundScores.get(playerId);

            int roundNum = s.getMatchRound() != null ? s.getMatchRound().getRoundNumber() : 0;
            userRounds.computeIfAbsent(roundNum, k -> new RoundScore());
            RoundScore rs = userRounds.get(roundNum);
            rs.getScores().put(s.getScoreType(), s.getScore() != null ? s.getScore().doubleValue() : 0.0);
        }

        for (Map.Entry<Long, Map<Integer, RoundScore>> entry : playerRoundScores.entrySet()) {
            Long pid = entry.getKey();
            List<RoundScore> sortedRounds = entry.getValue().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            PlayerScore ps = new PlayerScore();
            ps.setRoundScores(sortedRounds);
            result.addPlayerResult(pid, ps);
        }

        result.calculateWinner();
        return result;
    }
}
