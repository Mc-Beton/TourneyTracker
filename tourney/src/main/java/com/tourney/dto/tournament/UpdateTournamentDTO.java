package com.tourney.dto.tournament;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTournamentDTO {
    @NotBlank(message = "Nazwa turnieju nie może być pusta")
    private String name;

    @NotNull(message = "Data rozpoczęcia jest wymagana")
    @Future(message = "Data rozpoczęcia musi być w przyszłości")
    private LocalDate startDate;

    @Min(value = 30, message = "Czas trwania rundy musi być co najmniej 30 minut")
    private int roundDurationMinutes;
}
