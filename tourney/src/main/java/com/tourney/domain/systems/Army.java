package com.tourney.domain.systems;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "armies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Army {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "army_faction_id", nullable = false)
    private ArmyFaction armyFaction;
}
