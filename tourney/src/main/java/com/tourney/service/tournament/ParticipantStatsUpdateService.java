package com.tourney.service.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.TournamentMatch;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.scores.Score;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentScoring;
import com.tourney.repository.participant.TournamentParticipantRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.service.TournamentPointsCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serwis odpowiedzialny za aktualizację statystyk uczestników turnieju
 * po zakończeniu meczu.
 */
@Service
@RequiredArgsConstructor
public class ParticipantStatsUpdateService {

    private final TournamentParticipantRepository participantRepository;
    private final ScoreRepository scoreRepository;
    private final TournamentPointsCalculationService tournamentPointsCalculationService;

    /**
     * Aktualizuje statystyki obu graczy po zakończeniu meczu turnieju
     * 
     * @param match zakończony mecz
     */
    @Transactional
    public void updateStatsAfterMatch(Match match) {
        if (!(match instanceof TournamentMatch)) {
            return; // Nie dotyczy meczów spoza turnieju
        }

        TournamentMatch tournamentMatch = (TournamentMatch) match;
        Tournament tournament = tournamentMatch.getTournamentRound().getTournament();
        TournamentScoring scoring = tournament.getTournamentScoring();

        if (scoring == null || scoring.getTournamentPointsSystem() == null) {
            // Brak konfiguracji punktacji - pomijamy aktualizację
            return;
        }

        // Mecz typu BYE - tylko jeden gracz
        if (match.getPlayer1() == null || match.getPlayer2() == null) {
            handleByeMatch(match, tournament);
            return;
        }

        Long player1Id = match.getPlayer1().getId();
        Long player2Id = match.getPlayer2().getId();

        // Pobierz uczestników
        TournamentParticipant participant1 = participantRepository
                .findByTournamentIdAndUserId(tournament.getId(), player1Id)
                .orElse(null);
        
        TournamentParticipant participant2 = participantRepository
                .findByTournamentIdAndUserId(tournament.getId(), player2Id)
                .orElse(null);

        if (participant1 == null || participant2 == null) {
            return; // Nie można zaktualizować - gracze nie są uczestnikami
        }

        // Oblicz małe punkty (Score Points)
        long player1ScorePoints = calculatePlayerScorePoints(match, player1Id);
        long player2ScorePoints = calculatePlayerScorePoints(match, player2Id);

        // Oblicz duże punkty (Tournament Points)
        int player1TP = tournamentPointsCalculationService.calculateTournamentPoints(
                player1ScorePoints, player2ScorePoints, scoring);
        
        int player2TP = tournamentPointsCalculationService.calculateTournamentPoints(
                player2ScorePoints, player1ScorePoints, scoring);

        // Określ wynik meczu (W/D/L)
        TournamentParticipant.MatchResult result1 = determineMatchResult(player1TP, player2TP);
        TournamentParticipant.MatchResult result2 = determineMatchResult(player2TP, player1TP);

        // Aktualizuj statystyki obu uczestników
        participant1.addMatchResult(player1TP, player1ScorePoints, result1);
        participant2.addMatchResult(player2TP, player2ScorePoints, result2);

        participantRepository.save(participant1);
        participantRepository.save(participant2);
    }

    /**
     * Obsługuje mecz typu BYE (jeden gracz dostaje walkower)
     */
    private void handleByeMatch(Match match, Tournament tournament) {
        if (match.getPlayer1() == null) {
            return; // Nie ma gracza - pomijamy
        }

        Long playerId = match.getPlayer1().getId();
        TournamentParticipant participant = participantRepository
                .findByTournamentIdAndUserId(tournament.getId(), playerId)
                .orElse(null);

        if (participant == null) {
            return;
        }

        // BYE - gracz nie dostaje punktów (organizator może to zmienić w przyszłości)
        // Zwiększamy tylko matchesPlayed
        participant.setMatchesPlayed(participant.getMatchesPlayed() + 1);
        participantRepository.save(participant);
    }

    /**
     * Oblicza sumę małych punktów (Score Points) dla gracza w meczu
     */
    private long calculatePlayerScorePoints(Match match, Long playerId) {
        List<Score> scores = scoreRepository.findAllByMatchIdWithRound(match.getId());
        
        return scores.stream()
                .filter(score -> {
                    if (score.getUser() != null && score.getUser().getId().equals(playerId)) {
                        return true;
                    }
                    // Określ stronę na podstawie pozycji gracza w meczu
                    boolean isPlayer1 = match.getPlayer1() != null && match.getPlayer1().getId().equals(playerId);
                    return (isPlayer1 && score.getSide() == com.tourney.domain.scores.MatchSide.PLAYER1) ||
                           (!isPlayer1 && score.getSide() == com.tourney.domain.scores.MatchSide.PLAYER2);
                })
                .mapToLong(Score::getScore)
                .sum();
    }

    /**
     * Określa wynik meczu na podstawie Tournament Points
     */
    private TournamentParticipant.MatchResult determineMatchResult(int playerTP, int opponentTP) {
        if (playerTP > opponentTP) {
            return TournamentParticipant.MatchResult.WIN;
        } else if (playerTP < opponentTP) {
            return TournamentParticipant.MatchResult.LOSS;
        } else {
            return TournamentParticipant.MatchResult.DRAW;
        }
    }

    /**
     * Przelicza statystyki dla wszystkich uczestników turnieju od nowa
     * (użyteczne przy migracji lub naprawie danych)
     * 
     * @param tournament turniej do przeliczenia
     */
    @Transactional
    public void recalculateAllStats(Tournament tournament) {
        // Resetuj statystyki wszystkich uczestników
        tournament.getParticipantLinks().forEach(TournamentParticipant::resetStats);
        
        // Przelicz każdy zakończony mecz
        tournament.getRounds().forEach(round -> 
            round.getMatches().stream()
                    .filter(Match::isCompleted)
                    .forEach(this::updateStatsAfterMatch)
        );
    }
}
