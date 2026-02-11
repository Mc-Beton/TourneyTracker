package com.tourney.dto.tournament;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UpdateTournamentDTO {
    @NotBlank(message = "Nazwa turnieju nie może być pusta")
    private String name;
    
    private String description;

    @NotNull(message = "Data rozpoczęcia jest wymagana")
    @FutureOrPresent(message = "Data rozpoczęcia nie może być w przeszłości")
    private LocalDate startDate;
    
    private LocalDate endDate;

    @Min(value = 30, message = "Czas trwania rundy musi być co najmniej 30 minut")
    private int roundDurationMinutes;
    
    private Integer scoreSubmissionExtraMinutes;
    
    private Integer maxParticipants;
    
    private LocalDateTime registrationDeadline;
    
    private String location;
    
    private String venue;
    
    private Integer armyPointsLimit;
}