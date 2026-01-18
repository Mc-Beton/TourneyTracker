package com.tourney.domain.games;

import com.tourney.domain.systems.Deployment;
import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.systems.PrimaryMission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "match_details")
public class MatchDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_system_id", nullable = false)
    private GameSystem gameSystem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_mission_id")
    private PrimaryMission primaryMission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_id")
    private Deployment deployment;

    private Integer armyPower;

    @Column(name = "match_name", length = 200)
    private String matchName;

    private String guestPlayer2Name;

    private Long firstPlayerId;

    @Enumerated(EnumType.STRING)
    private MatchMode mode = MatchMode.LIVE;

}