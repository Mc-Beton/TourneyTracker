package com.tourney.service.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.scores.Score;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentScoring;
import com.tourney.dto.tournament.ParticipantStatsDTO;
import com.tourney.dto.tournament.PodiumDTO;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.service.TournamentPointsCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TournamentStatsService {
    
    private final ScoreRepository scoreRepository;
    private final TournamentPointsCalculationService tournamentPointsCalculationService;

    /**
     * Oblicza statystyki wszystkich uczestników turnieju
     */
    public List<ParticipantStatsDTO> calculateParticipantStats(Tournament tournament) {
        Map<Long, ParticipantStatsDTO.ParticipantStatsDTOBuilder> statsMap = new HashMap<>();
        
        // Inicjalizacja dla wszystkich potwierdzonych uczestników
        tournament.getParticipantLinks().stream()
                .filter(TournamentParticipant::isConfirmed)
                .forEach(participant -> {
                    statsMap.put(
                        participant.getUser().getId(),
                        ParticipantStatsDTO.builder()
                                .userId(participant.getUser().getId())
                                .userName(participant.getUser().getName())
                                .wins(0)
                                .draws(0)
                                .losses(0)
                                .tournamentPoints(0)
                                .scorePoints(0L)
                                .matchesPlayed(0)
                    );
                });

        TournamentScoring scoring = tournament.getTournamentScoring();
        
        // Przetwarzanie meczów
        tournament.getRounds().forEach(round -> 
            round.getMatches().stream()
                    .filter(Match::isCompleted)
                    .forEach(match -> processMatch(match, statsMap, scoring))
        );

        // Sortowanie według TP, potem małych punktów
        return statsMap.values().stream()
                .map(ParticipantStatsDTO.ParticipantStatsDTOBuilder::build)
                .sorted(Comparator
                        .comparingInt(ParticipantStatsDTO::getTournamentPoints).reversed()
                        .thenComparingLong(ParticipantStatsDTO::getScorePoints).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Oblicza podium (top 3)
     */
    public PodiumDTO calculatePodium(Tournament tournament) {
        List<ParticipantStatsDTO> stats = calculateParticipantStats(tournament);
        
        return PodiumDTO.builder()
                .first(stats.size() > 0 ? stats.get(0) : null)
                .second(stats.size() > 1 ? stats.get(1) : null)
                .third(stats.size() > 2 ? stats.get(2) : null)
                .build();
    }

    /**
     * Przetwarza pojedynczy mecz i aktualizuje statystyki
     */
    private void processMatch(
            Match match,
            Map<Long, ParticipantStatsDTO.ParticipantStatsDTOBuilder> statsMap,
            TournamentScoring scoring
    ) {
        if (match.getPlayer1() == null || match.getPlayer2() == null) {
            // Bye match - gracz 1 dostaje walkower
            if (match.getPlayer1() != null) {
                ParticipantStatsDTO.ParticipantStatsDTOBuilder stats = statsMap.get(match.getPlayer1().getId());
                if (stats != null) {
                    // Zwiększ tylko matchesPlayed, nie liczę jako win/draw/loss
                    // Organizator może zdecydować inaczej w przyszłości
                }
            }
            return;
        }

        Long player1Id = match.getPlayer1().getId();
        Long player2Id = match.getPlayer2().getId();

        ParticipantStatsDTO.ParticipantStatsDTOBuilder stats1 = statsMap.get(player1Id);
        ParticipantStatsDTO.ParticipantStatsDTOBuilder stats2 = statsMap.get(player2Id);

        if (stats1 == null || stats2 == null) {
            return; // Gracze nie są uczestnikami (nie powinno się zdarzyć)
        }

        // Pobierz małe punkty
        long player1ScorePoints = calculatePlayerScorePoints(match, player1Id);
        long player2ScorePoints = calculatePlayerScorePoints(match, player2Id);

        // Oblicz duże punkty
        int player1TP = scoring != null && scoring.getTournamentPointsSystem() != null
                ? tournamentPointsCalculationService.calculateTournamentPoints(
                        player1ScorePoints, player2ScorePoints, scoring)
                : 0;
        
        int player2TP = scoring != null && scoring.getTournamentPointsSystem() != null
                ? tournamentPointsCalculationService.calculateTournamentPoints(
                        player2ScorePoints, player1ScorePoints, scoring)
                : 0;

        // Aktualizuj statystyki gracza 1
        updatePlayerStats(stats1, player1TP, player2TP, player1ScorePoints);
        
        // Aktualizuj statystyki gracza 2
        updatePlayerStats(stats2, player2TP, player1TP, player2ScorePoints);
    }

    /**
     * Aktualizuje statystyki pojedynczego gracza
     */
    private void updatePlayerStats(
            ParticipantStatsDTO.ParticipantStatsDTOBuilder stats,
            int playerTP,
            int opponentTP,
            long playerScorePoints
    ) {
        // Zwiększ mecze
        stats.matchesPlayed(stats.build().getMatchesPlayed() + 1);
        
        // Dodaj punkty
        stats.tournamentPoints(stats.build().getTournamentPoints() + playerTP);
        stats.scorePoints(stats.build().getScorePoints() + playerScorePoints);
        
        // Określ wynik (W/D/L) na podstawie Tournament Points
        if (playerTP > opponentTP) {
            stats.wins(stats.build().getWins() + 1);
        } else if (playerTP == opponentTP) {
            stats.draws(stats.build().getDraws() + 1);
        } else {
            stats.losses(stats.build().getLosses() + 1);
        }
    }

    /**
     * Oblicza sumę małych punktów gracza w meczu
     */
    private long calculatePlayerScorePoints(Match match, Long playerId) {
        return scoreRepository.findAllByMatchIdWithRound(match.getId()).stream()
                .filter(score -> score.getUser().getId().equals(playerId))
                .mapToLong(Score::getScore)
                .sum();
    }
}
