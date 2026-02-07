package com.tourney.dto.tournament;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchPairDTO {
    private Long matchId;
    private int tableNumber;
    
    private Long player1Id;
    private String player1Name;
    private Integer player1TournamentPoints; // duże punkty gracza 1
    
    private Long player2Id;
    private String player2Name;
    private Integer player2TournamentPoints; // duże punkty gracza 2
    
    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED
    private LocalDateTime startTime;
    private LocalDateTime gameEndTime; // actual end time, null until match finishes
    private int gameDurationMinutes; // planned duration
    private LocalDateTime resultSubmissionDeadline;
    
    private boolean scoresSubmitted; // czy obaj gracze wpisali punkty
    
    // Wyniki meczów (suma punktów ze wszystkich rund)
    private Long player1TotalScore; // suma małych punktów (main + secondary score)
    private Long player2TotalScore; // suma małych punktów
    private String matchWinner; // PLAYER1, PLAYER2, DRAW, null jeśli nie zakończony
}
