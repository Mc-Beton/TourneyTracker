package com.tourney.service;

import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.TournamentPointsSystem;
import com.tourney.domain.tournament.TournamentScoring;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentPointsCalculationService {

    /**
     * Oblicza Tournament Points (duże punkty) dla jednego gracza na podstawie wyniku meczu
     * 
     * @param playerScorePoints suma wszystkich Score Points gracza (suma małych punktów)
     * @param opponentScorePoints suma wszystkich Score Points przeciwnika
     * @param tournamentScoring konfiguracja systemu punktacji turnieju
     * @return liczba Tournament Points dla gracza
     */
    public int calculateTournamentPoints(
            long playerScorePoints,
            long opponentScorePoints,
            TournamentScoring tournamentScoring
    ) {
        TournamentPointsSystem system = tournamentScoring.getTournamentPointsSystem();
        if (system == null) {
            throw new IllegalStateException("Brak ustawionego systemu Tournament Points");
        }

        long difference = playerScorePoints - opponentScorePoints;

        return switch (system) {
            case FIXED -> calculateFixedPoints(difference, tournamentScoring);
            case POINT_DIFFERENCE_STRICT -> calculateStrictPoints(difference);
            case POINT_DIFFERENCE_LENIENT -> calculateLenientPoints(difference);
        };
    }

    /**
     * System FIXED - stała punktacja (np. 3-1-0)
     * Remis = różnica 0
     */
    private int calculateFixedPoints(long difference, TournamentScoring scoring) {
        if (difference == 0) {
            return scoring.getPointsForDraw() != null ? scoring.getPointsForDraw() : 1;
        } else if (difference > 0) {
            return scoring.getPointsForWin() != null ? scoring.getPointsForWin() : 3;
        } else {
            return scoring.getPointsForLoss() != null ? scoring.getPointsForLoss() : 0;
        }
    }

    /**
     * System POINT_DIFFERENCE_STRICT - standardowy
     * Przedziały co 5 punktów:
     * - 0: 10:10 (remis)
     * - 1-5: 11:9
     * - 6-10: 12:8
     * - 11-15: 13:7
     * - 16-20: 14:6
     * - 21-25: 15:5
     * - 26-30: 16:4
     * - 31-35: 17:3
     * - 36-40: 18:2
     * - 41-45: 19:1
     * - 46+: 20:0
     */
    private int calculateStrictPoints(long difference) {
        long absDiff = Math.abs(difference);
        int basePoints = 10;
        
        if (absDiff == 0) {
            return basePoints; // 10:10
        } else if (absDiff <= 5) {
            return difference > 0 ? 11 : 9; // 11:9 lub 9:11
        } else if (absDiff <= 10) {
            return difference > 0 ? 12 : 8; // 12:8 lub 8:12
        } else if (absDiff <= 15) {
            return difference > 0 ? 13 : 7; // 13:7 lub 7:13
        } else if (absDiff <= 20) {
            return difference > 0 ? 14 : 6; // 14:6 lub 6:14
        } else if (absDiff <= 25) {
            return difference > 0 ? 15 : 5; // 15:5 lub 5:15
        } else if (absDiff <= 30) {
            return difference > 0 ? 16 : 4; // 16:4 lub 4:16
        } else if (absDiff <= 35) {
            return difference > 0 ? 17 : 3; // 17:3 lub 3:17
        } else if (absDiff <= 40) {
            return difference > 0 ? 18 : 2; // 18:2 lub 2:18
        } else if (absDiff <= 45) {
            return difference > 0 ? 19 : 1; // 19:1 lub 1:19
        } else {
            return difference > 0 ? 20 : 0; // 20:0 lub 0:20
        }
    }

    /**
     * System POINT_DIFFERENCE_LENIENT - łagodny
     * Przedziały co 5 punktów:
     * - 0-5: 10:10 (remis)
     * - 6-10: 11:9
     * - 11-15: 12:8
     * - 16-20: 13:7
     * - 21-25: 14:6
     * - 26-30: 15:5
     * - 31-35: 16:4
     * - 36-40: 17:3
     * - 41-45: 18:2
     * - 46-50: 19:1
     * - 51+: 20:0
     */
    private int calculateLenientPoints(long difference) {
        long absDiff = Math.abs(difference);
        int basePoints = 10;
        
        if (absDiff <= 5) {
            return basePoints; // 10:10
        } else if (absDiff <= 10) {
            return difference > 0 ? 11 : 9; // 11:9 lub 9:11
        } else if (absDiff <= 15) {
            return difference > 0 ? 12 : 8; // 12:8 lub 8:12
        } else if (absDiff <= 20) {
            return difference > 0 ? 13 : 7; // 13:7 lub 7:13
        } else if (absDiff <= 25) {
            return difference > 0 ? 14 : 6; // 14:6 lub 6:14
        } else if (absDiff <= 30) {
            return difference > 0 ? 15 : 5; // 15:5 lub 5:15
        } else if (absDiff <= 35) {
            return difference > 0 ? 16 : 4; // 16:4 lub 4:16
        } else if (absDiff <= 40) {
            return difference > 0 ? 17 : 3; // 17:3 lub 3:17
        } else if (absDiff <= 45) {
            return difference > 0 ? 18 : 2; // 18:2 lub 2:18
        } else if (absDiff <= 50) {
            return difference > 0 ? 19 : 1; // 19:1 lub 1:19
        } else {
            return difference > 0 ? 20 : 0; // 20:0 lub 0:20
        }
    }

    /**
     * Oblicza sumę Score Points (małych punktów) dla gracza w danej rundzie
     */
    public long calculateTotalScorePoints(List<Score> scores) {
        return scores.stream()
                .mapToLong(Score::getScore)
                .sum();
    }
}
