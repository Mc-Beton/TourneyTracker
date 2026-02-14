package com.tourney.domain.tournament;

import com.tourney.domain.systems.Deployment;
import com.tourney.domain.systems.PrimaryMission;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tournament_round_definitions")
public class TournamentRoundDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;
    
    @Column(nullable = false)
    private Integer roundNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deployment_id")
    private Deployment deployment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_mission_id")
    private PrimaryMission primaryMission;
    
    @Column(nullable = false)
    private Boolean isSplitMapLayout = false;
    
    private String mapLayoutEven;
    
    private String mapLayoutOdd;
    
    @Column(nullable = false)
    private Integer byeLargePoints = 0;
    
    @Column(nullable = false)
    private Integer byeSmallPoints = 0;
    
    @Column(nullable = false)
    private Integer splitLargePoints = 0;
    
    @Column(nullable = false)
    private Integer splitSmallPoints = 0;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "pairing_algorithm")
    private PairingAlgorithmType pairingAlgorithm = PairingAlgorithmType.STANDARD;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "player_level_pairing_strategy")
    private PlayerLevelPairingStrategy playerLevelPairingStrategy = PlayerLevelPairingStrategy.NONE;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "table_assignment_strategy")
    private TableAssignmentStrategy tableAssignmentStrategy = TableAssignmentStrategy.BEST_FIRST;
}
