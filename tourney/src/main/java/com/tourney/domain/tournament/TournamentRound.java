package com.tourney.domain.tournament;

import com.tourney.domain.games.Match;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class TournamentRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int roundNumber;

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @OneToMany(mappedBy = "tournamentRound", cascade = CascadeType.ALL)
    private List<Match> matches = new ArrayList<>();
}
