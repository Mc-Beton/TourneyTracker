package com.tourney.service.player;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.matches.CurrentMatchDTO;
import com.tourney.dto.matches.MatchStatusDTO;
import com.tourney.dto.player.OpponentStatusDTO;
import com.tourney.dto.tournament.ActiveTournamentDTO;
import com.tourney.exception.TournamentException;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.MatchException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.tourney.exception.TournamentErrorCode.TOURNAMENT_NOT_FOUND;


@Service
@Transactional
@RequiredArgsConstructor
public class PlayerMatchService {
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ScoreRepository scoreRepository;

    private Match getCurrentMatchForPlayer(Tournament tournament, Long playerId) {
        return matchRepository.findByTournamentAndPlayer(
                tournament.getId(), 
                playerId, 
                tournament.getCurrentRound()
        );
    }

    private String getOpponentName(Match match, Long playerId) {
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
        if (currentMatch.getStatus() == MatchStatus.SCHEDULED
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
        if (!match.isPlayerInMatch(playerId)) {
            throw new MatchException(PLAYER_NOT_IN_MATCH, 
                "Gracz nie jest uczestnikiem tego meczu");
        }
    }

    private void validateResultsSubmitted(Match match) {
        if (!match.areResultsSubmitted()) {
            throw new MatchException(RESULTS_NOT_SUBMITTED, 
                "Wyniki meczu nie zostały jeszcze wprowadzone");
        }
    }

    private List<RoundScoreDTO> getRoundScores(Match match, Long playerId) {
        List<MatchRound> rounds = match.getRounds();
        return rounds.stream()
                .map(round -> {
                    Score playerScore = scoreRepository.findByMatchRoundAndPlayer(
                            round.getId(), 
                            playerId
                    );
                    Score opponentScore = scoreRepository.findByMatchRoundAndPlayer(
                            round.getId(), 
                            match.getOpponentId(playerId)
                    );
                    
                    return RoundScoreDTO.builder()
                            .roundNumber(round.getRoundNumber())
                            .playerScore(convertScore(playerScore))
                            .opponentScore(convertScore(opponentScore))
                            .isSubmitted(playerScore != null)
                            .isConfirmed(round.isConfirmed())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<ScoreType, Integer> convertScore(Score score) {
        if (score == null) {
            return Collections.emptyMap();
        }
        return score.getScores();
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
            throw new MatchException(MATCH_NOT_FOUND, 
                "Nie znaleziono aktywnego meczu dla gracza w tym turnieju");
        }

        return CurrentMatchDTO.builder()
                .matchId(match.getId())
                .opponentName(getOpponentName(match, playerId))
                .status(match.getStatus())
                .startTime(match.getStartTime())
                .endTime(match.getEndTime())
                .isReady(match.isPlayerReady(playerId))
                .opponentReady(match.isOpponentReady(playerId))
                .resultsSubmitted(match.areResultsSubmitted())
                .resultsConfirmed(match.areResultsConfirmed())
                .rounds(getRoundScores(match, playerId))
                .build();
    }

    @Transactional
    public MatchStatusDTO reportPlayerReady(Long matchId, Long playerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchException(MATCH_NOT_FOUND, 
                    "Nie znaleziono meczu"));

        validatePlayerInMatch(match, playerId);
        
        match.setPlayerReady(playerId);
        
        // Jeśli obaj gracze są gotowi, rozpocznij mecz
        if (match.areBothPlayersReady()) {
            match.setStatus(MatchStatus.IN_PROGRESS);
            match.setStartTime(LocalDateTime.now());
        }

        match = matchRepository.save(match);

        return MatchStatusDTO.builder()
                .matchId(match.getId())
                .status(match.getStatus())
                .player1Ready(match.isPlayer1Ready())
                .player2Ready(match.isPlayer2Ready())
                .lastStatusUpdate(LocalDateTime.now())
                .build();
    }

    @Transactional
    public MatchResultConfirmationDTO confirmOpponentResult(Long matchId, Long playerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchException(MATCH_NOT_FOUND, 
                    "Nie znaleziono meczu"));

        validatePlayerInMatch(match, playerId);
        validateResultsSubmitted(match);

        match.confirmResults(playerId);
        
        if (match.areBothPlayersConfirmed()) {
            match.setStatus(MatchStatus.COMPLETED);
            match.setEndTime(LocalDateTime.now());
        }

        match = matchRepository.save(match);

        return MatchResultConfirmationDTO.builder()
                .matchId(match.getId())
                .isConfirmed(true)
                .completionTime(match.getEndTime())
                .build();
    }

    public OpponentStatusDTO getOpponentStatus(Long matchId, Long playerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchException(MATCH_NOT_FOUND, 
                    "Nie znaleziono meczu"));

        validatePlayerInMatch(match, playerId);
        Long opponentId = match.getOpponentId(playerId);

        return OpponentStatusDTO.builder()
                .opponentName(getOpponentName(match, playerId))
                .isReady(match.isPlayerReady(opponentId))
                .hasSubmittedResults(match.hasPlayerSubmittedResults(opponentId))
                .lastActivity(match.getLastActivityTime(opponentId))
                .build();
    }
}