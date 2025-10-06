package com.tourney.domain.tournament;

import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.user.User;
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
    
    @OneToOne
    @JoinColumn(name = "game_system_id")
    private GameSystem gameSystem;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToMany
    @JoinTable(
            name = "tournament_participants",
            joinColumns = @JoinColumn(name = "tournament_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundNumber ASC")
    private List<TournamentRound> rounds = new ArrayList<>();

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
               (maxParticipants == null || participants.size() < maxParticipants) &&
               (registrationDeadline == null || LocalDateTime.now().isBefore(registrationDeadline)) &&
               status == TournamentStatus.DRAFT;
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