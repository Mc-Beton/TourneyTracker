package com.tourney.service.match;

import com.tourney.domain.games.*;
import com.tourney.domain.scores.MatchSide;
import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.systems.Deployment;
import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.systems.PrimaryMission;
import com.common.domain.User;
import com.tourney.dto.matches.CreateSingleMatchDTO;
import com.tourney.dto.matches.MatchDetailsDTO;
import com.tourney.dto.matches.MatchSummaryDTO;
import com.tourney.dto.rounds.RoundTableRowDTO;
import com.tourney.dto.scores.RoundScoreDTO;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.match.MatchDetailsRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.systems.DeploymentRepository;
import com.tourney.repository.systems.GameSystemRepository;
import com.tourney.repository.systems.PrimaryMissionRepository;
import com.tourney.repository.user.UserRepository;
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
public class SingleMatchService {

    private final MatchRepository matchRepository;
    private final MatchDetailsRepository matchDetailsRepository;

    private final UserRepository userRepository;
    private final GameSystemRepository gameSystemRepository;
    private final PrimaryMissionRepository primaryMissionRepository;
    private final DeploymentRepository deploymentRepository;
    private final ScoreRepository scoreRepository;

    public SingleMatch createSingleMatch(CreateSingleMatchDTO dto, Long currentUserId) {
        validate(dto, currentUserId);

        User player1 = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        User player2 = null;
        if (dto.getPlayer2Id() != null) {
            player2 = userRepository.findById(dto.getPlayer2Id())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
        }

        GameSystem gameSystem = gameSystemRepository.findById(dto.getGameSystemId())
                .orElseThrow(() -> new EntityNotFoundException("GameSystem not found"));

        PrimaryMission primaryMission = null;
        if (dto.getPrimaryMissionId() != null) {
            primaryMission = primaryMissionRepository.findById(dto.getPrimaryMissionId())
                    .orElseThrow(() -> new EntityNotFoundException("PrimaryMission not found"));
        }

        Deployment deployment = null;
        if (dto.getDeploymentId() != null) {
            deployment = deploymentRepository.findById(dto.getDeploymentId())
                    .orElseThrow(() -> new EntityNotFoundException("Deployment not found"));
        }

        SingleMatch match = new SingleMatch();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setStatus(MatchStatus.SCHEDULED);

        // Utwórz rundy na podstawie GameSystem.defaultRoundNumber (bez czasów)
        int totalRounds = Math.max(1, gameSystem.getDefaultRoundNumber());

        var rounds = new ArrayList<MatchRound>(totalRounds);
        for (int i = 1; i <= totalRounds; i++) {
            MatchRound r = new MatchRound();
            r.setMatch(match);
            r.setRoundNumber(i);
            r.setStartTime(null);
            r.setEndTime(null);
            rounds.add(r);
        }
        match.setRounds(rounds);

        // zapisze też rounds dzięki cascade=ALL na Match.rounds
        match = matchRepository.save(match);

        // NIE tworzymy Score przy tworzeniu - dopiero przy rozpoczęciu meczu
        // Score będą utworzone w startSingleMatch() po zatwierdzeniu gotowości graczy

        MatchDetails details = new MatchDetails();
        details.setMatch(match);
        details.setMatchName(StringUtils.hasText(dto.getMatchName()) ? dto.getMatchName().trim() : null);
        details.setGameSystem(gameSystem);
        details.setMode(dto.getMode() != null ? dto.getMode() : MatchMode.LIVE);
        details.setPrimaryMission(primaryMission);
        details.setDeployment(deployment);
        details.setArmyPower(dto.getArmyPower());
        details.setGuestPlayer2Name(StringUtils.hasText(dto.getGuestPlayer2Name()) ? dto.getGuestPlayer2Name().trim() : null);
        details.setFirstPlayerId(dto.getFirstPlayerId());

        details = matchDetailsRepository.save(details);
        match.setDetails(details);

        return match;
    }

    /**
     * Inicjalizuje mecz pojedynczy - tworzy Score dla wszystkich rund.
     * Wywoływane przy rozpoczęciu meczu (po zatwierdzeniu gotowości).
     */
    public void startSingleMatch(Long matchId, Long startingUserId) {
        SingleMatch match = matchRepository.findById(matchId)
                .map(m -> m instanceof SingleMatch ? (SingleMatch) m : null)
                .orElseThrow(() -> new EntityNotFoundException("SingleMatch not found with id: " + matchId));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new IllegalStateException("Mecz można rozpocząć tylko ze statusu SCHEDULED");
        }

        GameSystem gameSystem = match.getDetails() != null ? match.getDetails().getGameSystem() : null;
        if (gameSystem == null) {
            throw new IllegalStateException("Mecz nie ma przypisanego systemu gry");
        }

        // Utwórz Score dla wszystkich rund
        List<Score> scores = buildInitialScores(match, gameSystem, startingUserId);
        scoreRepository.saveAll(scores);

        // Status zostanie zmieniony na IN_PROGRESS w PlayerMatchService.startMatch
    }

    private List<Score> buildInitialScores(Match match, GameSystem gameSystem, Long enteredByUserId) {
        List<MatchRound> rounds = match.getRounds();
        if (rounds == null || rounds.isEmpty()) {
            return List.of();
        }

        // Wymaga metody helper w GameSystem: getEnabledScoreTypes()
        List<ScoreType> enabledTypes = gameSystem.getEnabledScoreTypes();
        if (enabledTypes == null || enabledTypes.isEmpty()) {
            return List.of();
        }

        int expectedSize = rounds.size() * 2 * enabledTypes.size();
        List<Score> out = new ArrayList<>(expectedSize);

        LocalDateTime now = LocalDateTime.now();

        for (MatchRound round : rounds) {
            for (MatchSide side : List.of(MatchSide.PLAYER1, MatchSide.PLAYER2)) {
                for (ScoreType type : enabledTypes) {
                    Score s = new Score();
                    s.setMatchRound(round);
                    s.setUser(null); // hot-seat dopuszczalny; identyfikacja po side
                    s.setSide(side);
                    s.setScoreType(type);
                    s.setScore(0L);

                    s.setEnteredByUserId(enteredByUserId);
                    s.setEnteredAt(now);
                    s.setUpdatedAt(now);

                    out.add(s);
                }
            }
        }

        return out;
    }

    public List<SingleMatch> getMySingleMatches(Long currentUserId) {
        return matchRepository.findMySingleMatches(currentUserId);
    }

    private void validate(CreateSingleMatchDTO dto, Long currentUserId) {
        boolean hasRegisteredOpponent = dto.getPlayer2Id() != null;
        boolean hasGuestOpponentName = StringUtils.hasText(dto.getGuestPlayer2Name());

        if (hasRegisteredOpponent == hasGuestOpponentName) {
            throw new IllegalArgumentException("Podaj albo player2Id (gracz w systemie), albo guestPlayer2Name (hot-seat).");
        }

        if (dto.getPlayer2Id() != null && dto.getPlayer2Id().equals(currentUserId)) {
            throw new IllegalArgumentException("player2Id nie może być równe id aktualnego użytkownika.");
        }
    }

    public MatchDetailsDTO getMatchDetails(Long matchId, Long currentUserId) {
        Match match = matchRepository.findMatchDetailsView(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        validateParticipant(match, currentUserId);

        User opponent = resolveOpponent(match, currentUserId);
        String opponentName = resolveOpponentName(match, opponent);

        // wszystkie Score dla tego matcha (dla obu graczy i wszystkich rund)
        List<Score> scores = scoreRepository.findAllByMatchIdWithRoundAndUser(matchId);

        // indeks: roundId -> (userId -> (scoreType -> value))
        Map<Long, Map<Long, Map<ScoreType, Integer>>> byRoundThenUserThenType = scores.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getMatchRound().getId(),
                        Collectors.groupingBy(
                                s -> s.getUser().getId(),
                                Collectors.toMap(
                                        Score::getScoreType,
                                        s -> safeLongToInt(s.getScore()),
                                        Integer::sum,
                                        () -> new EnumMap<>(ScoreType.class)
                                )
                        )
                ));

        // Dla flag isSubmitted/isConfirmed: w Twoich DTO to już jest, ale w modelu
        // nie widzę per-runda potwierdzeń. Na razie ustawiamy na bazie MatchResult.
        boolean submitted = match.hasPlayerSubmittedResults(currentUserId);
        boolean confirmed = match.isPlayerConfirmed(currentUserId);

        List<RoundScoreDTO> roundDtos = match.getRounds().stream()
                .sorted(Comparator.comparingInt(MatchRound::getRoundNumber))
                .map(round -> {
                    Map<Long, Map<ScoreType, Integer>> perUser = byRoundThenUserThenType.getOrDefault(round.getId(), Map.of());

                    Map<ScoreType, Integer> playerMap = perUser.getOrDefault(currentUserId, Map.of());
                    Long opponentId = opponent != null ? opponent.getId() : null;
                    Map<ScoreType, Integer> opponentMap = opponentId != null
                            ? perUser.getOrDefault(opponentId, Map.of())
                            : Map.of();

                    return RoundScoreDTO.builder()
                            .roundNumber(round.getRoundNumber())
                            .playerScore(playerMap)
                            .opponentScore(opponentMap)
                            .isSubmitted(submitted)
                            .isConfirmed(confirmed)
                            .build();
                })
                .toList();

        String matchName = match.getDetails() != null ? match.getDetails().getMatchName() : null;

        return MatchDetailsDTO.builder()
                .matchId(match.getId())
                .matchName(matchName)
                .startTime(match.getStartTime())
                .mode(match.getDetails() != null ? match.getDetails().getMode() : null)
                .opponentId(opponent != null ? opponent.getId() : null)
                .opponentName(opponentName)
                .roundScores(roundDtos)
                .build();
    }

    private void validateParticipant(Match match, Long currentUserId) {
        boolean isP1 = match.getPlayer1() != null && Objects.equals(match.getPlayer1().getId(), currentUserId);
        boolean isP2 = match.getPlayer2() != null && Objects.equals(match.getPlayer2().getId(), currentUserId);
        if (!isP1 && !isP2) {
            throw new IllegalArgumentException("Brak dostępu do szczegółów tej rozgrywki.");
        }
    }

    private User resolveOpponent(Match match, Long currentUserId) {
        if (match.getPlayer1() != null && Objects.equals(match.getPlayer1().getId(), currentUserId)) {
            return match.getPlayer2();
        }
        if (match.getPlayer2() != null && Objects.equals(match.getPlayer2().getId(), currentUserId)) {
            return match.getPlayer1();
        }
        return null;
    }

    private String resolveOpponentName(Match match, User opponent) {
        if (opponent != null) {
            return opponent.getName();
        }
        // hot-seat: player2 == null -> nazwa w MatchDetails
        String guest = match.getDetails() != null ? match.getDetails().getGuestPlayer2Name() : null;
        return StringUtils.hasText(guest) ? guest : null;
    }

    public MatchSummaryDTO getMatchSummary(Long matchId, Long currentUserId) {
        Match match = matchRepository.findMatchSummaryView(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        validateParticipant(match, currentUserId);

        String matchName = match.getDetails() != null ? match.getDetails().getMatchName() : null;

        boolean ready = match.isPlayerReady(currentUserId);
        boolean opponentReady = match.isOpponentReady(currentUserId);

        String player1Name = match.getPlayer1() != null ? match.getPlayer1().getName() : null;
        String player2Name = resolvePlayer2DisplayName(match);

        // Extract tournamentId if this is a TournamentMatch
        Long tournamentId = null;
        if (match instanceof com.tourney.domain.games.TournamentMatch) {
            com.tourney.domain.games.TournamentMatch tournamentMatch = (com.tourney.domain.games.TournamentMatch) match;
            if (tournamentMatch.getTournamentRound() != null && tournamentMatch.getTournamentRound().getTournament() != null) {
                tournamentId = tournamentMatch.getTournamentRound().getTournament().getId();
            }
        }

        String primaryMission = match.getDetails() != null && match.getDetails().getPrimaryMission() != null
                ? match.getDetails().getPrimaryMission().getName()
                : null;

        String deployment = match.getDetails() != null && match.getDetails().getDeployment() != null
                ? match.getDetails().getDeployment().getName()
                : null;

        Integer armyPower = match.getDetails() != null ? match.getDetails().getArmyPower() : null;

        GameSystem gs = (match.getDetails() != null) ? match.getDetails().getGameSystem() : null;
        // For legacy matches without GameSystem, enable all score types by default
        boolean primaryScoreEnabled = gs != null ? gs.isPrimaryScoreEnabled() : true;
        boolean secondaryScoreEnabled = gs != null ? gs.isSecondaryScoreEnabled() : true;
        boolean thirdScoreEnabled = gs != null ? gs.isThirdScoreEnabled() : false;
        boolean additionalScoreEnabled = gs != null ? gs.isAdditionalScoreEnabled() : false;

        List<Score> scores = scoreRepository.findAllByMatchIdWithRound(matchId);

        Map<Integer, EnumMap<MatchSide, EnumMap<ScoreType, Integer>>> byRound = new HashMap<>();

        for (Score s : scores) {
            int roundNumber = s.getMatchRound().getRoundNumber();
            byRound.computeIfAbsent(roundNumber, rn -> new EnumMap<>(MatchSide.class));

            EnumMap<MatchSide, EnumMap<ScoreType, Integer>> bySide = byRound.get(roundNumber);
            bySide.computeIfAbsent(s.getSide(), side -> new EnumMap<>(ScoreType.class));

            EnumMap<ScoreType, Integer> byType = bySide.get(s.getSide());
            byType.merge(s.getScoreType(), safeLongToInt(s.getScore()), Integer::sum);
        }

        List<RoundTableRowDTO> roundRows = match.getRounds().stream()
                .sorted(Comparator.comparingInt(MatchRound::getRoundNumber))
                .map(r -> {
                    EnumMap<MatchSide, EnumMap<ScoreType, Integer>> perSide =
                            byRound.getOrDefault(r.getRoundNumber(), new EnumMap<>(MatchSide.class));

                    Map<ScoreType, Integer> p1 = perSide.getOrDefault(MatchSide.PLAYER1, new EnumMap<>(ScoreType.class));
                    Map<ScoreType, Integer> p2 = perSide.getOrDefault(MatchSide.PLAYER2, new EnumMap<>(ScoreType.class));

                    return RoundTableRowDTO.builder()
                            .roundNumber(r.getRoundNumber())
                            .player1(withAllTypes(p1))
                            .player2(withAllTypes(p2))
                            .build();
                })
                .toList();

        EnumMap<ScoreType, Integer> totalP1 = new EnumMap<>(ScoreType.class);
        EnumMap<ScoreType, Integer> totalP2 = new EnumMap<>(ScoreType.class);
        for (RoundTableRowDTO row : roundRows) {
            sumInto(totalP1, row.getPlayer1());
            sumInto(totalP2, row.getPlayer2());
        }

        Map<String, Map<ScoreType, Integer>> totalsByPlayerAndType = new LinkedHashMap<>();
        totalsByPlayerAndType.put("P1", totalP1);
        totalsByPlayerAndType.put("P2", totalP2);

        Map<String, Integer> totalPointsByPlayer = new LinkedHashMap<>();
        totalPointsByPlayer.put("P1", totalP1.values().stream().mapToInt(Integer::intValue).sum());
        totalPointsByPlayer.put("P2", totalP2.values().stream().mapToInt(Integer::intValue).sum());

        return MatchSummaryDTO.builder()
                .matchId(match.getId())
                .matchName(matchName)
                .tournamentId(tournamentId)
                .currentUserId(currentUserId)
                .player1Id(match.getPlayer1() != null ? match.getPlayer1().getId() : null)
                .player2Id(match.getPlayer2() != null ? match.getPlayer2().getId() : null)
                .player1Name(player1Name)
                .player2Name(player2Name)
                .primaryMission(primaryMission)
                .deployment(deployment)
                .armyPower(armyPower)
                .startTime(match.getStartTime())
                .endTime(match.getGameEndTime())
                .ready(ready)
                .opponentReady(opponentReady)
                .rounds(roundRows)
                .totalsByPlayerAndType(totalsByPlayerAndType)
                .totalPointsByPlayer(totalPointsByPlayer)
                .primaryScoreEnabled(primaryScoreEnabled)
                .secondaryScoreEnabled(secondaryScoreEnabled)
                .thirdScoreEnabled(thirdScoreEnabled)
                .additionalScoreEnabled(additionalScoreEnabled)
                .build();
    }

    @Transactional
    public MatchSummaryDTO finishMatch(Long matchId, Long currentUserId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        validateParticipant(match, currentUserId);

        // Idempotencja: jeśli już zakończony, zwróć aktualne summary
        if (match.getStatus() != MatchStatus.COMPLETED) {
            match.setStatus(MatchStatus.COMPLETED);
        }
        if (match.getGameEndTime() == null) {
            match.setGameEndTime(LocalDateTime.now());
        }

        matchRepository.save(match);

        // Zwróć pełne podsumowanie (już masz gotowe mapowanie + flagi scoreEnabled)
        return getMatchSummary(matchId, currentUserId);
    }

    private String resolvePlayer2DisplayName(Match match) {
        User p2 = match.getPlayer2();
        if (p2 != null) {
            return p2.getName();
        }
        String guest = match.getDetails() != null ? match.getDetails().getGuestPlayer2Name() : null;
        return guest != null && !guest.isBlank() ? guest.trim() : null;
    }

    private Map<ScoreType, Integer> withAllTypes(Map<ScoreType, Integer> in) {
        EnumMap<ScoreType, Integer> out = new EnumMap<>(ScoreType.class);
        for (ScoreType t : ScoreType.values()) {
            out.put(t, in.getOrDefault(t, 0));
        }
        return out;
    }

    private void sumInto(EnumMap<ScoreType, Integer> target, Map<ScoreType, Integer> add) {
        for (ScoreType t : ScoreType.values()) {
            target.merge(t, add.getOrDefault(t, 0), Integer::sum);
        }
    }

    private int safeLongToInt(Long v) {
        if (v == null) return 0;
        if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (v < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return v.intValue();
    }

}