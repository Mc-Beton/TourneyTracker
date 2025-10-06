package com.tourney.service.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.games.RoundStatus;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.dto.rounds.RoundCompletionSummaryDTO;
import com.tourney.exception.RoundNotCompletedException;
import com.tourney.exception.TournamentException;
import com.tourney.exception.TournamentNotFoundException;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.tournament.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.tourney.exception.TournamentErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentRoundService {
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;


    public int getNextRoundNumber(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));

        return tournament.getRounds().stream()
                .mapToInt(round -> round.getRoundNumber())
                .max()
                .orElse(0) + 1;
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

}