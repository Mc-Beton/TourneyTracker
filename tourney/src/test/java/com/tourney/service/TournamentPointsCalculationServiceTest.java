package com.tourney.service;

import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.TournamentPointsSystem;
import com.tourney.domain.tournament.TournamentScoring;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TournamentPointsCalculationServiceTest {

    @InjectMocks
    private TournamentPointsCalculationService service;

    private TournamentScoring fixedScoring;
    private TournamentScoring strictScoring;
    private TournamentScoring lenientScoring;

    @BeforeEach
    void setUp() {
        // Fixed system setup (3-1-0)
        fixedScoring = new TournamentScoring();
        fixedScoring.setTournamentPointsSystem(TournamentPointsSystem.FIXED);
        fixedScoring.setPointsForWin(3);
        fixedScoring.setPointsForDraw(1);
        fixedScoring.setPointsForLoss(0);

        // Strict system setup
        strictScoring = new TournamentScoring();
        strictScoring.setTournamentPointsSystem(TournamentPointsSystem.POINT_DIFFERENCE_STRICT);

        // Lenient system setup
        lenientScoring = new TournamentScoring();
        lenientScoring.setTournamentPointsSystem(TournamentPointsSystem.POINT_DIFFERENCE_LENIENT);
    }

    // ===== FIXED SYSTEM TESTS =====

    @Test
    void testCalculateTournamentPoints_Fixed_Win() {
        // Given
        long playerPoints = 100;
        long opponentPoints = 80;

        // When
        int result = service.calculateTournamentPoints(playerPoints, opponentPoints, fixedScoring);

        // Then
        assertEquals(3, result, "Win should give 3 points");
    }

    @Test
    void testCalculateTournamentPoints_Fixed_Draw() {
        // Given
        long playerPoints = 100;
        long opponentPoints = 100;

        // When
        int result = service.calculateTournamentPoints(playerPoints, opponentPoints, fixedScoring);

        // Then
        assertEquals(1, result, "Draw should give 1 point");
    }

    @Test
    void testCalculateTournamentPoints_Fixed_Loss() {
        // Given
        long playerPoints = 80;
        long opponentPoints = 100;

        // When
        int result = service.calculateTournamentPoints(playerPoints, opponentPoints, fixedScoring);

        // Then
        assertEquals(0, result, "Loss should give 0 points");
    }

    // ===== STRICT SYSTEM TESTS =====

    @Test
    void testCalculateTournamentPoints_Strict_ExactDraw() {
        // Given
        long playerPoints = 100;
        long opponentPoints = 100;

        // When
        int result = service.calculateTournamentPoints(playerPoints, opponentPoints, strictScoring);

        // Then
        assertEquals(10, result, "Exact draw should give 10:10");
    }

    @Test
    void testCalculateTournamentPoints_Strict_MinorWin_1Point() {
        // When
        int result = service.calculateTournamentPoints(100, 99, strictScoring);

        // Then
        assertEquals(11, result, "1 point difference should give 11:9");
    }

    @Test
    void testCalculateTournamentPoints_Strict_MinorWin_5Points() {
        // When
        int result = service.calculateTournamentPoints(100, 95, strictScoring);

        // Then
        assertEquals(11, result, "5 points difference should give 11:9");
    }

    @Test
    void testCalculateTournamentPoints_Strict_Win_6to10Points() {
        // When
        int result = service.calculateTournamentPoints(100, 94, strictScoring);

        // Then
        assertEquals(12, result, "6 points difference should give 12:8");
    }

    @Test
    void testCalculateTournamentPoints_Strict_Win_11to15Points() {
        // When
        int result = service.calculateTournamentPoints(100, 85, strictScoring);

        // Then
        assertEquals(13, result, "15 points difference should give 13:7");
    }

    @Test
    void testCalculateTournamentPoints_Strict_Win_16to20Points() {
        // When
        int result = service.calculateTournamentPoints(100, 80, strictScoring);

        // Then
        assertEquals(14, result, "20 points difference should give 14:6");
    }

    @Test
    void testCalculateTournamentPoints_Strict_Win_21PlusPoints() {
        // When
        int result = service.calculateTournamentPoints(100, 79, strictScoring);

        // Then
        assertEquals(15, result, "21+ points difference should give 15:5");
    }

    @Test
    void testCalculateTournamentPoints_Strict_Loss_1Point() {
        // When
        int result = service.calculateTournamentPoints(99, 100, strictScoring);

        // Then
        assertEquals(9, result, "1 point loss should give 9:11");
    }

    @Test
    void testCalculateTournamentPoints_Strict_Loss_21PlusPoints() {
        // When
        int result = service.calculateTournamentPoints(50, 100, strictScoring);

        // Then
        assertEquals(5, result, "Big loss should give 5:15");
    }

    // ===== LENIENT SYSTEM TESTS =====

    @Test
    void testCalculateTournamentPoints_Lenient_DrawRange_0Points() {
        // When
        int result = service.calculateTournamentPoints(100, 100, lenientScoring);

        // Then
        assertEquals(10, result, "0 point difference should give 10:10");
    }

    @Test
    void testCalculateTournamentPoints_Lenient_DrawRange_5Points() {
        // When
        int result = service.calculateTournamentPoints(100, 95, lenientScoring);

        // Then
        assertEquals(10, result, "5 points difference should give 10:10 in lenient");
    }

    @Test
    void testCalculateTournamentPoints_Lenient_Win_6to10Points() {
        // When
        int result = service.calculateTournamentPoints(100, 90, lenientScoring);

        // Then
        assertEquals(11, result, "10 points difference should give 11:9");
    }

    @Test
    void testCalculateTournamentPoints_Lenient_Win_11to15Points() {
        // When
        int result = service.calculateTournamentPoints(100, 85, lenientScoring);

        // Then
        assertEquals(12, result, "15 points difference should give 12:8");
    }

    @Test
    void testCalculateTournamentPoints_Lenient_Win_16to20Points() {
        // When
        int result = service.calculateTournamentPoints(100, 80, lenientScoring);

        // Then
        assertEquals(13, result, "20 points difference should give 13:7");
    }

    @Test
    void testCalculateTournamentPoints_Lenient_Win_21to25Points() {
        // When
        int result = service.calculateTournamentPoints(100, 75, lenientScoring);

        // Then
        assertEquals(14, result, "25 points difference should give 14:6");
    }

    @Test
    void testCalculateTournamentPoints_Lenient_Win_26PlusPoints() {
        // When
        int result = service.calculateTournamentPoints(100, 74, lenientScoring);

        // Then
        assertEquals(15, result, "26+ points difference should give 15:5");
    }

    @Test
    void testCalculateTournamentPoints_Lenient_Loss_WithinDrawRange() {
        // When
        int result = service.calculateTournamentPoints(95, 100, lenientScoring);

        // Then
        assertEquals(10, result, "Loss within 5 points should still be draw in lenient");
    }

    // ===== EDGE CASES =====

    @Test
    void testCalculateTournamentPoints_NoSystemSet_ThrowsException() {
        // Given
        TournamentScoring noSystem = new TournamentScoring();
        noSystem.setTournamentPointsSystem(null);

        // Then
        assertThrows(IllegalStateException.class, () -> {
            service.calculateTournamentPoints(100, 80, noSystem);
        });
    }

    @Test
    void testCalculateTournamentPoints_Fixed_WithNullValues_UsesDefaults() {
        // Given
        TournamentScoring nullValues = new TournamentScoring();
        nullValues.setTournamentPointsSystem(TournamentPointsSystem.FIXED);
        // null values for points

        // When
        int win = service.calculateTournamentPoints(100, 80, nullValues);
        int draw = service.calculateTournamentPoints(100, 100, nullValues);
        int loss = service.calculateTournamentPoints(80, 100, nullValues);

        // Then
        assertEquals(3, win, "Default win should be 3");
        assertEquals(1, draw, "Default draw should be 1");
        assertEquals(0, loss, "Default loss should be 0");
    }

    @Test
    void testCalculateTournamentPoints_LargeNumbers() {
        // When
        int result = service.calculateTournamentPoints(999999L, 100000L, strictScoring);

        // Then
        assertEquals(15, result, "Very large difference should give max points");
    }

    // ===== SCORE POINTS CALCULATION TESTS =====

    @Test
    void testCalculateTotalScorePoints_WithMultipleScores() {
        // Given
        List<Score> scores = new ArrayList<>();
        Score score1 = mock(Score.class);
        Score score2 = mock(Score.class);
        Score score3 = mock(Score.class);
        
        when(score1.getScore()).thenReturn(10L);
        when(score2.getScore()).thenReturn(20L);
        when(score3.getScore()).thenReturn(30L);
        
        scores.add(score1);
        scores.add(score2);
        scores.add(score3);

        // When
        long total = service.calculateTotalScorePoints(scores);

        // Then
        assertEquals(60L, total);
    }

    @Test
    void testCalculateTotalScorePoints_WithEmptyList() {
        // Given
        List<Score> scores = new ArrayList<>();

        // When
        long total = service.calculateTotalScorePoints(scores);

        // Then
        assertEquals(0L, total);
    }

    @Test
    void testCalculateTotalScorePoints_WithSingleScore() {
        // Given
        List<Score> scores = new ArrayList<>();
        Score score = mock(Score.class);
        when(score.getScore()).thenReturn(50L);
        scores.add(score);

        // When
        long total = service.calculateTotalScorePoints(scores);

        // Then
        assertEquals(50L, total);
    }

    @Test
    void testCalculateTotalScorePoints_WithZeroScores() {
        // Given
        List<Score> scores = new ArrayList<>();
        Score score1 = mock(Score.class);
        Score score2 = mock(Score.class);
        
        when(score1.getScore()).thenReturn(0L);
        when(score2.getScore()).thenReturn(0L);
        
        scores.add(score1);
        scores.add(score2);

        // When
        long total = service.calculateTotalScorePoints(scores);

        // Then
        assertEquals(0L, total);
    }
}
