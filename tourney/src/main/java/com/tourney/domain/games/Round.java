package com.tourney.domain.games;

import com.tourney.domain.Tournament;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int roundNumber;

    @ManyToOne
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL)
    private List<Match> matches = new ArrayList<>();
}
