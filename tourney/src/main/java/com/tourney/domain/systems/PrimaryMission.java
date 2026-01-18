package com.tourney.domain.systems;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "primary_missions")
public class PrimaryMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_system_id", nullable = false)
    private GameSystem gameSystem;

    @Column(nullable = false)
    private String name;
}