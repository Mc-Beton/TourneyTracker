package com.tourney.service.player;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchRound;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.games.TournamentMatch;
import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.games.MatchResultConfirmationDTO;
import com.tourney.dto.matches.CurrentMatchDTO;
import com.tourney.dto.matches.MatchStatusDTO;
import com.tourney.dto.player.OpponentStatusDTO;
import com.tourney.dto.scores.RoundScoreDTO;
import com.tourney.dto.tournament.ActiveTournamentDTO;
import com.tourney.exception.MatchOperationException;
import com.tourney.exception.TournamentException;
import com.tourney.exception.domain.MatchErrorCode;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.repository.user.UserRepository;
import com.tourney.service.tournament.TournamentRoundService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NonUniqueResultException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tourney.exception.TournamentErrorCode.*;
import static com.tourney.exception.domain.MatchErrorCode.*;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlayerMatchService {
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ScoreRepository scoreRepository;
    private final com.tourney.service.match.SingleMatchService singleMatchService;
    private final TournamentRoundService tournamentRoundService;
    private final com.tourney.service.tournament.ParticipantStatsUpdateService participantStatsUpdateService;

    public Match getCurrentMatchForPlayer(Tournament tournament, Long playerId) {
        Long tournamentId = tournament != null ? tournament.getId() : null;
        Integer currentRound = tournament != null ? tournament.getCurrentRound() : null;
        log.debug("[CurrentMatch] Enter getCurrentMatchForPlayer tid={}, pid={}, currentRound={}", tournamentId, playerId, currentRound);

        // 1) Spróbuj znaleźć w bieżącej rundzie – repo zwraca listę posortowaną malejąco po id
        List<com.tourney.domain.games.TournamentMatch> inCurrentRound =
                matchRepository.findAllByTournamentAndPlayerInRound(
                        tournamentId,
                        playerId,
                        currentRound
                );
        log.debug("[CurrentMatch] inCurrentRound size={} (tid={}, pid={}, round={})", inCurrentRound != null ? inCurrentRound.size() : 0, tournamentId, playerId, currentRound);

        // Wybór preferowanego meczu: najpierw IN_PROGRESS, potem SCHEDULED, w obu przypadkach najwyższe id
        if (inCurrentRound != null && !inCurrentRound.isEmpty()) {
            com.tourney.domain.games.TournamentMatch pick = inCurrentRound.stream()
                    .filter(m -> m.getStatus() == MatchStatus.IN_PROGRESS)
                    .findFirst()
                    .orElseGet(() -> inCurrentRound.stream()
                            .filter(m -> m.getStatus() == MatchStatus.SCHEDULED)
                            .findFirst()
                            .orElse(null));
            if (pick != null) {
                log.debug("[CurrentMatch] Picked from current round: matchId={}, status={} (tid={}, pid={})", pick.getId(), pick.getStatus(), tournamentId, playerId);
                return pick;
            }
        }

        // 2) Fallback: dowolny IN_PROGRESS w całym turnieju (ostatnia runda, największe id)
        List<com.tourney.domain.games.TournamentMatch> inProgressAnyRound =
                matchRepository.findInProgressForPlayerInTournament(tournamentId, playerId);
        log.debug("[CurrentMatch] inProgressAnyRound size={} (tid={}, pid={})", inProgressAnyRound != null ? inProgressAnyRound.size() : 0, tournamentId, playerId);
        if (inProgressAnyRound != null && !inProgressAnyRound.isEmpty()) {
            com.tourney.domain.games.TournamentMatch pick = inProgressAnyRound.get(0);
            log.debug("[CurrentMatch] Picked IN_PROGRESS from any round: matchId={}, status={} (round={})", pick.getId(), pick.getStatus(), pick.getTournamentRound() != null ? pick.getTournamentRound().getRoundNumber() : null);
            return pick;
        }

        // 3) Ostatnia szansa: jeśli w bieżącej rundzie coś jest (np. inny status), wybierz rekord o największym id
        if (inCurrentRound != null && !inCurrentRound.isEmpty()) {
            com.tourney.domain.games.TournamentMatch pick = inCurrentRound.get(0);
            log.debug("[CurrentMatch] Picked fallback from current round: matchId={}, status={}", pick.getId(), pick.getStatus());
            return pick;
        }

        // brak dopasowania
        log.debug("[CurrentMatch] No match found (tid={}, pid={})", tournamentId, playerId);
        return null;
    }

    public String getOpponentName(Match match, Long playerId) {
        if (match.getPlayer1().getId().equals(playerId)) {
            return match.getPlayer2().getName();
        }
        return match.getPlayer1().getName();
    }

    private boolean checkIfActionRequired(Tournament tournament, Long playerId) {
        Match currentMatch = getCurrentMatchForPlayer(tournament, playerId);
        if (currentMatch == null) {
            return false;
        }

        // Sprawdź czy gracz musi zgłosić gotowość
        // Dla meczów turniejowych sprawdzamy również IN_PROGRESS (runda może być w trakcie)
        boolean isTournamentMatch = currentMatch instanceof com.tourney.domain.games.TournamentMatch;
        if ((currentMatch.getStatus() == MatchStatus.SCHEDULED || 
             (currentMatch.getStatus() == MatchStatus.IN_PROGRESS && isTournamentMatch))
                && !currentMatch.isPlayerReady(playerId)) {
            return true;
        }

        // Sprawdź czy gracz musi wprowadzić wyniki
        if (currentMatch.getStatus() == MatchStatus.IN_PROGRESS
                && !currentMatch.hasPlayerSubmittedResults(playerId)) {
            return true;
        }

        // Sprawdź czy gracz musi potwierdzić wyniki przeciwnika
        return currentMatch.getStatus() == MatchStatus.IN_PROGRESS
                && currentMatch.needsConfirmationFrom(playerId);
    }

    private void validatePlayerInMatch(Match match, Long playerId) {
        if (!playerId.equals(match.getPlayer1().getId()) &&
                !playerId.equals(match.getPlayer2().getId())) {
            throw new MatchOperationException(MatchErrorCode.PLAYER_NOT_IN_MATCH);
        }
    }

    private void validateResultsSubmitted(Match match) {
        if (match.getMatchResult() == null ||
                match.getMatchResult().getSubmittedById() == null) {
            throw new MatchOperationException(MatchErrorCode.RESULTS_NOT_SUBMITTED);
        }
    }


    private List<RoundScoreDTO> getRoundScores(Match match, Long playerId) {
        List<MatchRound> rounds = match.getRounds();
        Long matchId = match.getId();
        log.debug("[CurrentMatch] Build round scores for matchId={}, playerId={}, rounds={} ", matchId, playerId, rounds != null ? rounds.size() : 0);

        return rounds.stream()
                .map(round -> {
                    Score playerScore = null;
                    Score opponentScore = null;
                    Long opponentId = null;
                    try {
                        opponentId = getOpponentId(match, playerId);
                    } catch (Exception e) {
                        log.warn("[CurrentMatch] Unable to resolve opponentId for matchId={}, playerId={}: {}", matchId, playerId, e.toString());
                    }

                    // Probe single-result fetch for player
                    try {
                        playerScore = scoreRepository.findByMatchRoundAndPlayerId(round, playerId);
                    } catch (NonUniqueResultException nure) {
                        // Diagnostic logging: count duplicates per type for this round + player
                        try {
                            final Long rId = round.getId();
                            final Long pId = playerId;
                            List<Score> all = scoreRepository.findAllByMatchIdWithRound(matchId);
                            long totalForPlayerAndRound = all.stream()
                                    .filter(s -> s.getMatchRound() != null && s.getMatchRound().getId().equals(rId))
                                    .filter(s -> s.getUser() != null && s.getUser().getId().equals(pId))
                                    .count();
                            Map<ScoreType, Long> byType = all.stream()
                                    .filter(s -> s.getMatchRound() != null && s.getMatchRound().getId().equals(rId))
                                    .filter(s -> s.getUser() != null && s.getUser().getId().equals(pId))
                                    .collect(Collectors.groupingBy(Score::getScoreType, Collectors.counting()));
                            log.error("[CurrentMatch][SCORES] NonUniqueResult for PLAYER (matchId={}, roundNo={}, roundId={}, playerId={}) duplicates={}, byType={}",
                                    matchId, round.getRoundNumber(), rId, pId, totalForPlayerAndRound, byType);
                        } catch (Exception ex) {
                            log.error("[CurrentMatch][SCORES] Failed to collect diagnostics for PLAYER (matchId={}, roundId={}, playerId={}): {}", matchId, round.getId(), playerId, ex.toString());
                        }
                        // Re-throw to preserve original behavior (na razie tylko logujemy problem)
                        throw nure;
                    }

                    // Probe single-result fetch for opponent (if exists)
                    if (opponentId != null) {
                        try {
                            opponentScore = scoreRepository.findByMatchRoundAndPlayerId(round, opponentId);
                        } catch (NonUniqueResultException nure) {
                            try {
                                final Long rId = round.getId();
                                final Long oppId = opponentId;
                                List<Score> all = scoreRepository.findAllByMatchIdWithRound(matchId);
                                long totalForOppAndRound = all.stream()
                                        .filter(s -> s.getMatchRound() != null && s.getMatchRound().getId().equals(rId))
                                        .filter(s -> s.getUser() != null && s.getUser().getId().equals(oppId))
                                        .count();
                                Map<ScoreType, Long> byType = all.stream()
                                        .filter(s -> s.getMatchRound() != null && s.getMatchRound().getId().equals(rId))
                                        .filter(s -> s.getUser() != null && s.getUser().getId().equals(oppId))
                                        .collect(Collectors.groupingBy(Score::getScoreType, Collectors.counting()));
                                log.error("[CurrentMatch][SCORES] NonUniqueResult for OPPONENT (matchId={}, roundNo={}, roundId={}, opponentId={}) duplicates={}, byType={}",
                                        matchId, round.getRoundNumber(), rId, oppId, totalForOppAndRound, byType);
                            } catch (Exception ex) {
                                log.error("[CurrentMatch][SCORES] Failed to collect diagnostics for OPPONENT (matchId={}, roundId={}, opponentId={}): {}", matchId, round.getId(), opponentId, ex.toString());
                            }
                            throw nure;
                        }
                    }

                    boolean submitted = playerScore != null;
                    log.debug("[CurrentMatch] Round {} (roundId={}) submitted={}, playerScoreType={}, oppScoreType={}",
                            round.getRoundNumber(), round.getId(), submitted,
                            playerScore != null ? playerScore.getScoreType() : null,
                            opponentScore != null ? opponentScore.getScoreType() : null);

                    return RoundScoreDTO.builder()
                            .roundNumber(round.getRoundNumber())
                            .playerScore(convertScore(playerScore))
                            .opponentScore(convertScore(opponentScore))
                            .isSubmitted(submitted)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<ScoreType, Integer> convertScore(Score score) {
        if (score == null) {
            return Collections.emptyMap();
        }
        return Map.of(score.getScoreType(), score.getScore().intValue());
    }

    private OffsetDateTime toOffset(LocalDateTime local) {
        if (local == null) return null;
        ZoneId zone = ZoneId.systemDefault();
        return local.atZone(zone).toOffsetDateTime();
    }

    public List<ActiveTournamentDTO> getActiveTournaments(Long playerId) {
        return tournamentRepository.findActiveForPlayer(playerId).stream()
                .map(tournament -> {
                    Match currentMatch = getCurrentMatchForPlayer(tournament, playerId);
                    return ActiveTournamentDTO.builder()
                            .tournamentId(tournament.getId())
                            .tournamentName(tournament.getName())
                            .currentRound(tournament.getCurrentRound())
                            .roundStartTime(toOffset(tournament.getCurrentRoundStartTime()))
                            .roundEndTime(toOffset(tournament.getCurrentRoundEndTime()))
                            .currentMatchStatus(currentMatch != null ? currentMatch.getStatus() : null)
                            .opponent(currentMatch != null ? getOpponentName(currentMatch, playerId) : null)
                            .requiresAction(checkIfActionRequired(tournament, playerId))
                            .build();
                })
                .collect(Collectors.toList());
    }

    public CurrentMatchDTO getCurrentMatch(Long tournamentId, Long playerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentException(TOURNAMENT_NOT_FOUND,
                    "Nie znaleziono turnieju"));

        Match match = getCurrentMatchForPlayer(tournament, playerId);
        if (match == null) {
            // Zamiast 500 zwracamy brak treści — kontroler zamieni null na 204 No Content
            return null;
        }

        return CurrentMatchDTO.builder()
                .matchId(match.getId())
                .tableNumber(match.getTableNumber())
                .opponentName(getOpponentName(match, playerId))
                .status(match.getStatus())
                .startTime(match.getStartTime())
                .endTime(match.getGameEndTime())
                .isReady(match.isPlayerReady(playerId))
                .opponentReady(match.isOpponentReady(playerId))
                .rounds(getRoundScores(match, playerId))
                .build();
    }

    @Transactional
    public MatchStatusDTO reportPlayerReady(Long matchId, Long playerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchOperationException(MATCH_NOT_FOUND));

        validatePlayerInMatch(match, playerId);

        // Dla meczów turniejowych pozwalamy na ready nawet gdy runda jest w trakcie,
        // o ile nie ma jeszcze wyników (faktyczna rozgrywka nie została rozpoczęta)
        boolean isTournamentMatch = match instanceof com.tourney.domain.games.TournamentMatch;
        
        // Dla pojedynczych meczów: mogą być w SCHEDULED (przed rozpoczęciem)
        // Dla turniejowych: mogą być w IN_PROGRESS (runda w trakcie) jeśli nie ma jeszcze wyników
        if (match.getStatus() == MatchStatus.IN_PROGRESS && !isTournamentMatch) {
            throw new IllegalStateException("Nie można zmienić gotowości po rozpoczęciu rozgrywki.");
        }
        
        // Dla meczów turniejowych sprawdzamy czy są już wyniki
        if (isTournamentMatch) {
            boolean hasScores = scoreRepository.findAllByMatchIdWithRound(matchId).size() > 0;
            if (hasScores) {
                throw new IllegalStateException("Nie można zmienić gotowości - rozgrywka już trwa.");
            }
        }

        // hotseat: tylko player1 może zgłosić gotowość
        boolean isHotseat = match.getPlayer2() == null;
        if (isHotseat) {
            match.setPlayer1Ready(true);
            match.setPlayer2Ready(true);
        } else {
            match.setPlayerReady(playerId);
        }

        match = matchRepository.save(match);

        return MatchStatusDTO.builder()
                .matchId(match.getId())
                .status(match.getStatus()) // nadal SCHEDULED aż do /start
                .player1Ready(match.isPlayer1Ready())
                .player2Ready(match.isPlayer2Ready())
                .lastStatusUpdate(LocalDateTime.now())
                .build();
    }


    @Transactional
    public MatchResultConfirmationDTO confirmOpponentResult(Long matchId, Long playerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchOperationException(MATCH_NOT_FOUND));

        validatePlayerInMatch(match, playerId);
        validateResultsSubmitted(match);

        match.confirmResults(playerId);

        if (match.areBothPlayersConfirmed()) {
            match.setStatus(MatchStatus.COMPLETED);
            match.setGameEndTime(LocalDateTime.now());
            
            // Automatyczne sprawdzenie i zakończenie rundy turnieju jeśli wszystkie mecze są zakończone
            if (match instanceof TournamentMatch tournamentMatch) {
                try {
                    Long tournamentId = tournamentMatch.getTournamentRound().getTournament().getId();
                    int roundNumber = tournamentMatch.getTournamentRound().getRoundNumber();
                    
                    log.info("Mecz {} zakończony. Aktualizuję statystyki i sprawdzam rundę {} turnieju {}", 
                            matchId, roundNumber, tournamentId);
                    
                    // Aktualizuj statystyki uczestników
                    participantStatsUpdateService.updateStatsAfterMatch(match);
                    
                    // Sprawdź auto-zakończenie rundy
                    tournamentRoundService.autoCompleteRoundIfReady(tournamentId, roundNumber);
                } catch (Exception e) {
                    log.error("Błąd podczas aktualizacji statystyk lub kończenia rundy: {}", e.getMessage(), e);
                    // Nie przerywamy operacji - mecz został zakończony pomyślnie
                }
            }
        }

        match = matchRepository.save(match);

        return MatchResultConfirmationDTO.builder()
                .matchId(match.getId())
                .isConfirmed(true)
                .completionTime(match.getGameEndTime())
                .build();
    }

    public OpponentStatusDTO getOpponentStatus(Long matchId, Long playerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchOperationException(MATCH_NOT_FOUND));

        validatePlayerInMatch(match, playerId);
        Long opponentId = match.getOpponentId(playerId);

        return OpponentStatusDTO.builder()
                .opponentName(getOpponentName(match, playerId))
                .isReady(match.isPlayerReady(opponentId))
                .hasSubmittedResults(match.hasPlayerSubmittedResults(opponentId))
                .build();
    }

    private Long getOpponentId(Match match, Long playerId) {
        return match.getPlayer1().getId().equals(playerId)
                ? match.getPlayer2().getId()
                : match.getPlayer1().getId();
    }

    public MatchStatusDTO startMatch(Long matchId, Long currentUserId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match not found"));

        boolean isP1 = match.getPlayer1() != null && Objects.equals(match.getPlayer1().getId(), currentUserId);
        boolean isP2 = match.getPlayer2() != null && Objects.equals(match.getPlayer2().getId(), currentUserId);
        if (!isP1 && !isP2) {
            throw new IllegalArgumentException("Brak uprawnień do uruchomienia tej rozgrywki.");
        }

        // Sprawdź czy faktycznie rozgrywka już się rozpoczęła (są wyniki)
        boolean hasScores = scoreRepository.findAllByMatchIdWithRound(matchId).size() > 0;
        if (hasScores) {
            throw new IllegalStateException("Rozgrywka została już rozpoczęta.");
        }

        boolean isHotseat = match.getPlayer2() == null;
        if (isHotseat) {
            // wariant B: hotseat -> start bez wymogu ready, ale tylko player1
            if (!isP1) {
                throw new IllegalArgumentException("W trybie hotseat tylko gracz 1 może rozpocząć rozgrywkę.");
            }
        } else {
            // 2 zarejestrowanych graczy: obaj muszą być ready
            if (!match.areBothPlayersReady()) {
                throw new IllegalStateException("Nie można rozpocząć: obaj gracze muszą zgłosić gotowość (ready).");
            }
        }

        // Dla pojedynczych meczów: inicjalizuj Score przy rozpoczęciu
        boolean isSingleMatch = match instanceof com.tourney.domain.games.SingleMatch;
        if (isSingleMatch) {
            singleMatchService.startSingleMatch(matchId, currentUserId);
        }

        LocalDateTime now = LocalDateTime.now();
        match.setStartTime(now);
        match.setStatus(MatchStatus.IN_PROGRESS);

        matchRepository.save(match);

        return MatchStatusDTO.builder()
                .matchId(match.getId())
                .status(match.getStatus())
                .player1Ready(match.isPlayer1Ready())
                .player2Ready(match.isPlayer2Ready())
                .lastStatusUpdate(now)
                .build();
    }
}