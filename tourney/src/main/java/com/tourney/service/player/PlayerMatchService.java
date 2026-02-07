package com.tourney.service.player;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchRound;
import com.tourney.domain.games.MatchStatus;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
public class PlayerMatchService {
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ScoreRepository scoreRepository;
    private final com.tourney.service.match.SingleMatchService singleMatchService;

    public Match getCurrentMatchForPlayer(Tournament tournament, Long playerId) {
        return matchRepository.findByTournamentAndPlayer(
                tournament.getId(),
                playerId,
                tournament.getCurrentRound()
        );
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
        return rounds.stream()
                .map(round -> {
                    Score playerScore = scoreRepository.findByMatchRoundAndPlayerId(
                            round,
                            playerId
                    );
                    Score opponentScore = scoreRepository.findByMatchRoundAndPlayerId(
                            round,
                            getOpponentId(match, playerId)
                    );

                    return RoundScoreDTO.builder()
                            .roundNumber(round.getRoundNumber())
                            .playerScore(convertScore(playerScore))
                            .opponentScore(convertScore(opponentScore))
                            .isSubmitted(playerScore != null)
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

    public List<ActiveTournamentDTO> getActiveTournaments(Long playerId) {
        return tournamentRepository.findActiveForPlayer(playerId).stream()
                .map(tournament -> {
                    Match currentMatch = getCurrentMatchForPlayer(tournament, playerId);
                    return ActiveTournamentDTO.builder()
                            .tournamentId(tournament.getId())
                            .tournamentName(tournament.getName())
                            .currentRound(tournament.getCurrentRound())
                            .roundStartTime(tournament.getCurrentRoundStartTime())
                            .roundEndTime(tournament.getCurrentRoundEndTime())
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
            throw new MatchOperationException(MATCH_NOT_FOUND,
                "Nie znaleziono aktywnego meczu dla gracza w tym turnieju");
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