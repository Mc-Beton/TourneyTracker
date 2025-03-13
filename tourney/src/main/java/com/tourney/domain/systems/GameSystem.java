package com.tourney.domain.systems;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "gamesystems")
@Data
public abstract class GameSystem {

    @Id
    private Long id;

    @Column
    private String name;
}
