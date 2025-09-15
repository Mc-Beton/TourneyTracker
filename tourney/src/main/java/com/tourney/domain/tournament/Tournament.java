package com.tourney.domain.tournament;

import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    private LocalDate startDate;
    private int numberOfRounds;

    @OneToOne
    @JoinColumn(name = "game_system_id")
    private GameSystem gameSystem;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer; // Organizator turnieju

    @ManyToMany
    @JoinTable(
            name = "tournament_participants",
            joinColumns = @JoinColumn(name = "tournament_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>(); // Lista uczestników
}
