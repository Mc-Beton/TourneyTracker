package com.tourney.dto.tournament;

import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.scores.ScoringSystem;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class CreateTournamentDTO {
    @NotBlank(message = "Nazwa turnieju jest wymagana")
    private String name;
    
    private String description;
    
    @NotNull(message = "Data rozpoczęcia jest wymagana")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    @Min(value = 1, message = "Minimalna liczba rund to 1")
    private int numberOfRounds;
    
    @Min(value = 15, message = "Minimalna długość rundy to 15 minut")
    private int roundDurationMinutes;
    
    @NotNull(message = "System gry jest wymagany")
    private Long gameSystemId;
    
    private TournamentType type = TournamentType.SWISS;
    
    private Integer maxParticipants;
    
    private LocalDateTime registrationDeadline;
    
    private String location;
    private String venue;
    
    // Pola związane z systemem punktacji
    private ScoringSystem scoringSystem;
    private Set<ScoreType> enabledScoreTypes;
    private boolean requireAllScoreTypes;
    private Integer minScore;
    private Integer maxScore;
}