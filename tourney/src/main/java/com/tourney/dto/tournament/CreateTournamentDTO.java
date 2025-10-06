package com.tourney.dto.tournament;

import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.scores.ScoringSystem;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateTournamentDTO {
    @NotBlank(message = "Nazwa turnieju nie może być pusta")
    private String name;
    
    @NotNull(message = "Data rozpoczęcia jest wymagana")
    @Future(message = "Data rozpoczęcia musi być w przyszłości")
    private LocalDate startDate;
    
    @Min(value = 1, message = "Liczba rund musi być większa od 0")
    private int numberOfRounds;
    
    @Min(value = 90, message = "Czas trwania rundy musi być co najmniej 90 minut")
    private int roundDurationMinutes;
    
    @NotNull(message = "ID systemu gry jest wymagane")
    private Long gameSystemId;

    @NotNull(message = "System punktacji jest wymagany")
    private ScoringSystem scoringSystem;

    @NotEmpty(message = "Przynajmniej jeden typ punktacji musi być wybrany")
    private Set<ScoreType> enabledScoreTypes;

    private boolean requireAllScoreTypes;

    @Min(value = 0, message = "Minimalna liczba punktów nie może być ujemna")
    private Integer minScore;

    @Min(value = 0, message = "Maksymalna liczba punktów nie może być ujemna")
    private Integer maxScore;
}