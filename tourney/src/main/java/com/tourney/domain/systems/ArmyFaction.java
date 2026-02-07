package com.tourney.domain.systems;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "army_factions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArmyFaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_system_id", nullable = false)
    private GameSystem gameSystem;
}
