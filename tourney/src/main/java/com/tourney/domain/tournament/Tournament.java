package com.tourney.domain.tournament;

import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.systems.GameSystem;
import com.common.domain.User;
import com.tourney.dto.tournament.TournamentStatus;
import com.tourney.dto.tournament.TournamentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfRounds;
    private int roundDurationMinutes;
    
    // Czas dodatkowy (w minutach) na wpisanie punktów po zakończeniu meczu
    private Integer scoreSubmissionExtraMinutes = 15;
    
    // Tryb startowania meczów w rundzie
    @Enumerated(EnumType.STRING)
    private RoundStartMode roundStartMode = RoundStartMode.ALL_MATCHES_TOGETHER;
    
    @Enumerated(EnumType.STRING)
    private TournamentStatus status = TournamentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    private TournamentType type = TournamentType.SWISS;

    private Integer currentRound;
    private LocalDateTime currentRoundStartTime;
    private LocalDateTime currentRoundEndTime;
    
    private Integer maxParticipants;
    private Boolean registrationOpen = true;
    private LocalDateTime registrationDeadline;

    private String location;
    private String venue;
    
    // Limit punktów armii dla turnieju
    private Integer armyPointsLimit;
    
    @ManyToOne
    @JoinColumn(name = "game_system_id")
    private GameSystem gameSystem;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentParticipant> participantLinks = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundNumber ASC")
    private List<TournamentRound> rounds = new ArrayList<>();
    
    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundNumber ASC")
    private List<TournamentRoundDefinition> roundDefinitions = new ArrayList<>();

    @OneToOne(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    private TournamentScoring tournamentScoring;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = TournamentStatus.DRAFT;
        }
        if (currentRound == null) {
            currentRound = 0;
        }
        if (registrationOpen == null) {
            registrationOpen = true;
        }
    }

    public boolean canRegister() {
        return registrationOpen && 
               (maxParticipants == null || participantLinks.size() < maxParticipants) &&
               (registrationDeadline == null || LocalDateTime.now().isBefore(registrationDeadline)) &&
               status == TournamentStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == TournamentStatus.IN_PROGRESS && 
               currentRound != null && 
               currentRound > 0 && 
               currentRound <= numberOfRounds;
    }

    public boolean isFinished() {
        return status == TournamentStatus.COMPLETED;
    }

    public boolean canStartNextRound() {
        return status == TournamentStatus.IN_PROGRESS && 
               (currentRound == null || currentRound < numberOfRounds);
    }
}