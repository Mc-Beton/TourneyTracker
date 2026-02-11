package com.tourney.service.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchRound;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.games.RoundStatus;
import com.tourney.domain.games.TournamentMatch;
import com.tourney.domain.tournament.RoundStartMode;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentPhase;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.dto.rounds.RoundCompletionSummaryDTO;
import com.tourney.dto.tournament.MatchPairDTO;
import com.tourney.dto.tournament.RoundStatusDTO;
import com.tourney.dto.tournament.TournamentRoundViewDTO;
import com.tourney.exception.TournamentException;
import com.tourney.exception.TournamentNotFoundException;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.service.TournamentPointsCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.tourney.exception.TournamentErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TournamentRoundService {
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final ScoreRepository scoreRepository;
    private final TournamentPointsCalculationService tournamentPointsCalculationService;


    public int getNextRoundNumber(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        // Znajdź ostatnią zakończoną rundę (COMPLETED)
        // Następna runda to ta po ostatniej COMPLETED
        int lastCompletedRound = tournament.getRounds().stream()
                .filter(round -> round.getStatus() == RoundStatus.COMPLETED)
                .mapToInt(TournamentRound::getRoundNumber)
                .max()
                .orElse(0);

        return lastCompletedRound + 1;
    }


    public RoundCompletionSummaryDTO completeRound(Long tournamentId, int roundNumber) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentException(TOURNAMENT_NOT_FOUND,
                        "Nie znaleziono turnieju o ID: " + tournamentId));

        TournamentRound round = tournament.getRounds().stream()
                .filter(r -> r.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new TournamentException(ROUND_NOT_FOUND,
                        "Nie znaleziono rundy " + roundNumber + " w turnieju " + tournamentId));

        // Sprawdź czy wszystkie mecze są zakończone
        List<Match> matches = round.getMatches();
        List<Match> pendingMatches = matches.stream()
                .filter(match -> match.getStatus() != MatchStatus.COMPLETED)
                .toList();

        if (!pendingMatches.isEmpty()) {
            String pendingMatchesInfo = pendingMatches.stream()
                    .map(match -> String.format("Mecz %d: %s vs %s",
                            match.getId(),
                            match.getPlayer1().getName(),
                            match.getPlayer2().getName()))
                    .collect(Collectors.joining("\n"));

            return RoundCompletionSummaryDTO.builder()
                    .roundNumber(roundNumber)
                    .totalMatches(matches.size())
                    .completedMatches(matches.size() - pendingMatches.size())
                    .pendingMatches(pendingMatches.size())
                    .isCompleted(false)
                    .statusMessage("Nie wszystkie mecze zostały zakończone:\n" + pendingMatchesInfo)
                    .build();
        }

        // Oznacz rundę jako zakończoną
        round.setStatus(RoundStatus.COMPLETED);
        round.setCompletionTime(LocalDateTime.now());

        // Zmiana fazy turnieju
        if (roundNumber >= tournament.getNumberOfRounds()) {
            // To była ostatnia runda - turniej zakończony
            tournament.setPhase(TournamentPhase.TOURNAMENT_COMPLETE);
            tournament.setStatus(com.tourney.dto.tournament.TournamentStatus.COMPLETED);
        } else {
            // Są jeszcze kolejne rundy - czeka na dobranie par
            tournament.setPhase(TournamentPhase.AWAITING_PAIRINGS);
        }

        // Zapisz zmiany
        tournamentRepository.save(tournament);

        return RoundCompletionSummaryDTO.builder()
                .roundNumber(roundNumber)
                .totalMatches(matches.size())
                .completedMatches(matches.size())
                .pendingMatches(0)
                .isCompleted(true)
                .statusMessage("Runda została pomyślnie zakończona")
                .build();
    }

    public RoundCompletionSummaryDTO getRoundStatus(Long tournamentId, int roundNumber) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentException(TOURNAMENT_NOT_FOUND,
                        "Nie znaleziono turnieju o ID: " + tournamentId));

        TournamentRound round = tournament.getRounds().stream()
                .filter(r -> r.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new TournamentException(ROUND_NOT_FOUND,
                        "Nie znaleziono rundy " + roundNumber + " w turnieju " + tournamentId));

        List<Match> matches = round.getMatches();
        List<Match> pendingMatches = matches.stream()
                .filter(match -> match.getStatus() != MatchStatus.COMPLETED)
                .toList();

        return RoundCompletionSummaryDTO.builder()
                .roundNumber(roundNumber)
                .totalMatches(matches.size())
                .completedMatches(matches.size() - pendingMatches.size())
                .pendingMatches(pendingMatches.size())
                .isCompleted(pendingMatches.isEmpty() && round.getStatus() == RoundStatus.COMPLETED)
                .statusMessage(createStatusMessage(round, pendingMatches))
                .build();
    }

    /**
     * Automatycznie kończy rundę jeśli wszystkie mecze zostały zakończone
     */
    public void autoCompleteRoundIfReady(Long tournamentId, int roundNumber) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElse(null);
        
        if (tournament == null) {
            log.warn("Nie znaleziono turnieju o ID: {}", tournamentId);
            return;
        }

        TournamentRound round = tournament.getRounds().stream()
                .filter(r -> r.getRoundNumber() == roundNumber)
                .findFirst()
                .orElse(null);
                
        if (round == null) {
            log.warn("Nie znaleziono rundy {} w turnieju {}", roundNumber, tournamentId);
            return;
        }
        
        // Sprawdź czy runda jest już zakończona
        if (round.getStatus() == RoundStatus.COMPLETED) {
            log.debug("Runda {} w turnieju {} jest już zakończona", roundNumber, tournamentId);
            return;
        }

        // Sprawdź czy wszystkie mecze są zakończone
        List<Match> matches = round.getMatches();
        long completedCount = matches.stream()
                .filter(match -> match.getStatus() == MatchStatus.COMPLETED)
                .count();
        
        if (completedCount == matches.size() && !matches.isEmpty()) {
            // Wszystkie mecze zakończone - automatycznie zakończ rundę
            log.info("Wszystkie mecze zakończone w rundzie {} turnieju {}. Automatyczne kończenie rundy.", 
                    roundNumber, tournamentId);
            
            round.setStatus(RoundStatus.COMPLETED);
            round.setCompletionTime(LocalDateTime.now());
            
            // Zmiana fazy turnieju
            if (roundNumber >= tournament.getNumberOfRounds()) {
                // To była ostatnia runda - turniej zakończony
                tournament.setPhase(TournamentPhase.TOURNAMENT_COMPLETE);
                tournament.setStatus(com.tourney.dto.tournament.TournamentStatus.COMPLETED);
                log.info("Wszystkie rundy zakończone - turniej {} zakończony", tournamentId);
            } else {
                // Są jeszcze kolejne rundy - czeka na dobranie par
                tournament.setPhase(TournamentPhase.AWAITING_PAIRINGS);
                log.info("Runda zakończona, czeka na dobranie par do rundy {}", roundNumber + 1);
            }
            
            tournamentRepository.save(tournament);
            
            log.info("Runda {} w turnieju {} została automatycznie zakończona", roundNumber, tournamentId);
        } else {
            log.debug("Runda {} w turnieju {}: {}/{} meczów zakończonych", 
                    roundNumber, tournamentId, completedCount, matches.size());
        }
    }

    private String createStatusMessage(TournamentRound round, List<Match> pendingMatches) {
        if (round.getStatus() == RoundStatus.COMPLETED) {
            return "Runda zakończona o " + round.getCompletionTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        if (pendingMatches.isEmpty()) {
            return "Wszystkie mecze zakończone. Można zakończyć rundę.";
        }

        return "Oczekiwanie na zakończenie " + pendingMatches.size() + " meczy";
    }

    @Transactional(readOnly = true)
    public void validatePreviousRoundCompleted(Long tournamentId, int roundNumber) {
        if (roundNumber <= 1) {
            return; // Pierwsza runda nie wymaga walidacji
        }

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentException(TOURNAMENT_NOT_FOUND,
                        "Nie znaleziono turnieju o ID: " + tournamentId));

        TournamentRound previousRound = tournament.getRounds().stream()
                .filter(r -> r.getRoundNumber() == (roundNumber - 1))
                .findFirst()
                .orElseThrow(() -> new TournamentException(ROUND_NOT_FOUND,
                        "Nie znaleziono poprzedniej rundy dla rundy " + roundNumber));

        if (previousRound.getStatus() != RoundStatus.COMPLETED) {
            throw new TournamentException(INVALID_ROUND_STATE,
                    "Poprzednia runda (nr " + (roundNumber - 1) + ") nie została zakończona");
        }
    }

    /**
     * Rozpoczyna rundę - ustawia czasy dla meczów
     */
    public void startRound(Long tournamentId, int roundNumber) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        TournamentRound round = tournament.getRounds().stream()
                .filter(r -> r.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new TournamentException(ROUND_NOT_FOUND,
                        "Nie znaleziono rundy " + roundNumber));

        if (tournament.getRoundStartMode() == RoundStartMode.ALL_MATCHES_TOGETHER) {
            // Wszystkie mecze startują jednocześnie
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = now.plusMinutes(tournament.getRoundDurationMinutes());
            LocalDateTime deadline = endTime.plusMinutes(
                    tournament.getScoreSubmissionExtraMinutes() != null 
                            ? tournament.getScoreSubmissionExtraMinutes() 
                            : 15
            );

            round.getMatches().forEach(match -> {
                match.setStartTime(now);
                // gameEndTime is set only when match actually finishes, not at start
                match.setResultSubmissionDeadline(deadline);
                match.setGameDurationMinutes(tournament.getRoundDurationMinutes());
                match.setStatus(MatchStatus.IN_PROGRESS);
                
                // Automatycznie rozpocznij pierwszą rundę meczu turniejowego
                if (match.getRounds() != null && !match.getRounds().isEmpty()) {
                    MatchRound firstRound = match.getRounds().stream()
                            .filter(r -> r.getRoundNumber() == 1)
                            .findFirst()
                            .orElse(null);
                    if (firstRound != null && firstRound.getStartTime() == null) {
                        firstRound.setStartTime(now);
                    }
                }
            });

            round.setStatus(RoundStatus.IN_PROGRESS);
            tournament.setCurrentRound(roundNumber);
            tournament.setCurrentRoundStartTime(now);
            tournament.setCurrentRoundEndTime(endTime);
            
            // Zmiana fazy turnieju - runda aktywna
            tournament.setPhase(TournamentPhase.ROUND_ACTIVE);
        } else {
            // Mecze startują osobno - tylko przygotuj rundę
            round.setStatus(RoundStatus.NOT_STARTED);
            tournament.setCurrentRound(roundNumber);
        }

        tournamentRepository.save(tournament);
    }

    /**
     * Rozpoczyna pojedynczy mecz (dla trybu INDIVIDUAL_MATCHES)
     */
    public void startIndividualMatch(Long tournamentId, int roundNumber, Long matchId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        // Weryfikacja trybu startowania
        if (tournament.getRoundStartMode() != RoundStartMode.INDIVIDUAL_MATCHES) {
            throw new TournamentException(INVALID_TOURNAMENT_STATE,
                    "Turniej nie jest w trybie INDIVIDUAL_MATCHES");
        }

        TournamentRound round = tournament.getRounds().stream()
                .filter(r -> r.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new TournamentException(ROUND_NOT_FOUND,
                        "Nie znaleziono rundy " + roundNumber));

        Match match = round.getMatches().stream()
                .filter(m -> m.getId().equals(matchId))
                .findFirst()
                .orElseThrow(() -> new TournamentException(ROUND_NOT_FOUND,
                        "Nie znaleziono meczu o ID " + matchId));

        // Sprawdź czy mecz jest już rozpoczęty
        if (match.getStatus() == MatchStatus.IN_PROGRESS || match.getStatus() == MatchStatus.COMPLETED) {
            throw new TournamentException(INVALID_MATCH_STATE,
                    "Mecz jest już rozpoczęty lub zakończony");
        }

        // Rozpocznij mecz
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusMinutes(tournament.getRoundDurationMinutes());
        LocalDateTime deadline = endTime.plusMinutes(
                tournament.getScoreSubmissionExtraMinutes() != null 
                        ? tournament.getScoreSubmissionExtraMinutes() 
                        : 15
        );

        match.setStartTime(now);
        match.setResultSubmissionDeadline(deadline);
        match.setGameDurationMinutes(tournament.getRoundDurationMinutes());
        match.setStatus(MatchStatus.IN_PROGRESS);

        // Automatycznie rozpocznij pierwszą rundę meczu turniejowego
        if (match.getRounds() != null && !match.getRounds().isEmpty()) {
            MatchRound firstRound = match.getRounds().stream()
                    .filter(r -> r.getRoundNumber() == 1)
                    .findFirst()
                    .orElse(null);
            if (firstRound != null && firstRound.getStartTime() == null) {
                firstRound.setStartTime(now);
            }
        }

        // Jeśli to pierwszy mecz w rundzie, ustaw status rundy na IN_PROGRESS
        if (round.getStatus() == RoundStatus.NOT_STARTED) {
            round.setStatus(RoundStatus.IN_PROGRESS);
        }

        tournamentRepository.save(tournament);
    }

    /**
     * Przedłuża czas na wpisanie punktów
     */
    public void extendSubmissionDeadline(Long tournamentId, int roundNumber, int additionalMinutes) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        TournamentRound round = tournament.getRounds().stream()
                .filter(r -> r.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new TournamentException(ROUND_NOT_FOUND,
                        "Nie znaleziono rundy " + roundNumber));

        round.getMatches().forEach(match -> {
            if (match.getResultSubmissionDeadline() != null) {
                match.setResultSubmissionDeadline(
                        match.getResultSubmissionDeadline().plusMinutes(additionalMinutes)
                );
            }
        });

        tournamentRepository.save(tournament);
    }

    /**
     * Pobiera status rundy dla organizatora (sprawdza kto nie wpisał punktów)
     */
    @Transactional(readOnly = true)
    public RoundStatusDTO getRoundStatusForOrganizer(Long tournamentId, int roundNumber) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        TournamentRound round = tournament.getRounds().stream()
                .filter(r -> r.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new TournamentException(ROUND_NOT_FOUND,
                        "Nie znaleziono rundy " + roundNumber));

        List<String> playersWithoutScores = new ArrayList<>();
        int totalMatches = round.getMatches().size();
        int completedMatches = 0;

        for (Match match : round.getMatches()) {
            boolean player1HasScores = hasPlayerSubmittedScores(match, match.getPlayer1().getId());
            boolean player2HasScores = match.getPlayer2() != null 
                    && hasPlayerSubmittedScores(match, match.getPlayer2().getId());

            if (player1HasScores && (match.getPlayer2() == null || player2HasScores)) {
                completedMatches++;
            } else {
                if (!player1HasScores) {
                    playersWithoutScores.add(match.getPlayer1().getName());
                }
                if (match.getPlayer2() != null && !player2HasScores) {
                    playersWithoutScores.add(match.getPlayer2().getName());
                }
            }
        }

        return RoundStatusDTO.builder()
                .roundNumber(roundNumber)
                .allScoresSubmitted(playersWithoutScores.isEmpty())
                .playersWithoutScores(playersWithoutScores)
                .totalMatches(totalMatches)
                .completedMatches(completedMatches)
                .build();
    }

    /**
     * Pobiera widok wszystkich rund turnieju
     */
    @Transactional(readOnly = true)
    public List<TournamentRoundViewDTO> getTournamentRoundsView(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        return tournament.getRounds().stream()
                .sorted((r1, r2) -> Integer.compare(r1.getRoundNumber(), r2.getRoundNumber()))
                .map(round -> TournamentRoundViewDTO.builder()
                        .roundNumber(round.getRoundNumber())
                        .status(round.getStatus().name())
                        .matches(round.getMatches().stream()
                                .map(this::toMatchPairDTO)
                                .collect(Collectors.toList()))
                        .canStart(canStartRound(tournament, round))
                        .build())
                .collect(Collectors.toList());
    }

    private boolean canStartRound(Tournament tournament, TournamentRound round) {
        // Runda 1 może startować zawsze (jeśli pary są utworzone)
        if (round.getRoundNumber() == 1) {
            return !round.getMatches().isEmpty() && round.getStatus() == RoundStatus.IN_PROGRESS;
        }

        // Kolejne rundy wymagają zakończenia poprzedniej
        TournamentRound previousRound = tournament.getRounds().stream()
                .filter(r -> r.getRoundNumber() == round.getRoundNumber() - 1)
                .findFirst()
                .orElse(null);

        return previousRound != null 
                && previousRound.getStatus() == RoundStatus.COMPLETED
                && round.getStatus() == RoundStatus.IN_PROGRESS;
    }

    private MatchPairDTO toMatchPairDTO(Match match) {
        // Oblicz sumę punktów dla każdego gracza ze wszystkich rund
        var scores = scoreRepository.findAllByMatchIdWithRound(match.getId());
        
        long player1Total = scores.stream()
                .filter(s -> s.getSide() == com.tourney.domain.scores.MatchSide.PLAYER1)
                .mapToLong(s -> s.getScore() != null ? s.getScore() : 0L)
                .sum();
                
        long player2Total = scores.stream()
                .filter(s -> s.getSide() == com.tourney.domain.scores.MatchSide.PLAYER2)
                .mapToLong(s -> s.getScore() != null ? s.getScore() : 0L)
                .sum();
        
        // Określ zwycięzcę jeśli mecz zakończony
        String winner = null;
        if (match.getStatus() == MatchStatus.COMPLETED || match.isCompleted()) {
            if (player1Total > player2Total) {
                winner = "PLAYER1";
            } else if (player2Total > player1Total) {
                winner = "PLAYER2";
            } else {
                winner = "DRAW";
            }
        }
        
        // Oblicz Tournament Points dla zakończonych meczów turniejowych
        Integer player1TP = null;
        Integer player2TP = null;
        
        if (match instanceof TournamentMatch && match.getStatus() == MatchStatus.COMPLETED) {
            try {
                TournamentMatch tournamentMatch = (TournamentMatch) match;
                Tournament tournament = tournamentMatch.getTournamentRound().getTournament();
                
                // Oblicz Tournament Points na podstawie Score Points
                player1TP = tournamentPointsCalculationService.calculateTournamentPoints(
                    player1Total,
                    player2Total,
                    tournament.getTournamentScoring()
                );
                
                player2TP = tournamentPointsCalculationService.calculateTournamentPoints(
                    player2Total,
                    player1Total,
                    tournament.getTournamentScoring()
                );
            } catch (Exception e) {
                log.warn("Failed to calculate tournament points for match {}: {}", match.getId(), e.getMessage());
                player1TP = null;
                player2TP = null;
            }
        }
        
        return MatchPairDTO.builder()
                .matchId(match.getId())
                .tableNumber(match.getTableNumber() != null ? match.getTableNumber() : 0)
                .player1Id(match.getPlayer1().getId())
                .player1Name(match.getPlayer1().getName())
                .player1TournamentPoints(player1TP)
                .player2Id(match.getPlayer2() != null ? match.getPlayer2().getId() : null)
                .player2Name(match.getPlayer2() != null ? match.getPlayer2().getName() : "BYE")
                .player2TournamentPoints(player2TP)
                .status(match.getStatus().name())
                .startTime(match.getStartTime())
                .gameEndTime(match.getGameEndTime())
                .gameDurationMinutes(match.getGameDurationMinutes())
                .resultSubmissionDeadline(match.getResultSubmissionDeadline())
                .scoresSubmitted(match.isCompleted())
                .player1TotalScore(player1Total)
                .player2TotalScore(player2Total)
                .matchWinner(winner)
                .build();
    }

    private boolean hasPlayerSubmittedScores(Match match, Long playerId) {
        return scoreRepository.findAllByMatchIdWithRound(match.getId()).stream()
                .anyMatch(score -> score.getUser().getId().equals(playerId));
    }

}