package com.tourney.service.tournament;

import com.tourney.domain.games.*;
import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.tournament.*;
import com.common.domain.User;
import com.tourney.domain.team.Team;
import com.tourney.domain.team.TeamMember;
import com.tourney.domain.team.TeamMemberStatus;
import com.tourney.repository.TournamentRoundDefinitionRepository;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.team.TeamMemberRepository;
import com.tourney.repository.tournament.TournamentRepository;
import lombok.Getter;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive performance tests for pairing algorithms with various constraint scenarios:
 * - BYE handling (odd player counts)
 * - Same team constraints
 * - Different city constraints
 * - Combined constraints
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PairingAlgorithmConstraintPerformanceTest {

    /**
     * Constraint scenarios for testing different tournament configurations
     */
    enum ConstraintScenario {
        BYE_ONLY("BYE Handling", true, false, false),
        TEAM_ONLY("Same Team Constraint", false, true, false),
        CITY_ONLY("Different City Constraint", false, false, true),
        BYE_TEAM("BYE + Team", true, true, false),
        ALL_CONSTRAINTS("BYE + Team + City", true, true, true);
        
        final String description;
        final boolean hasBye;
        final boolean hasTeamConstraint;
        final boolean hasCityConstraint;
        
        ConstraintScenario(String description, boolean hasBye, boolean hasTeam, boolean hasCity) {
            this.description = description;
            this.hasBye = hasBye;
            this.hasTeamConstraint = hasTeam;
            this.hasCityConstraint = hasCity;
        }
    }

    @Mock
    private TournamentRepository tournamentRepository;
    
    @Mock
    private MatchRepository matchRepository;
    
    @Mock
    private ScoreRepository scoreRepository;
    
    @Mock
    private TournamentRoundDefinitionRepository roundDefinitionRepository;
    
    @Mock
    private TeamMemberRepository teamMemberRepository;

    private CPSATPairingService cpsatPairingService;
    private SubsequentRoundPairingService pairingService;
    
    private static final String[] CITIES = {"City A", "City B", "City C", "City D"};
    private static final Random RANDOM = new Random(42); // Fixed seed for reproducibility
    private static final int WARMUP_RUNS = 3;
    private static final int MEASUREMENT_RUNS = 5;
    
    private static final List<PerformanceResult> results = new ArrayList<>();
    
    private ThreadMXBean threadMXBean;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cpsatPairingService = new CPSATPairingService();
        pairingService = new SubsequentRoundPairingService(
                tournamentRepository,
                matchRepository,
                scoreRepository,
                roundDefinitionRepository,
                teamMemberRepository,
                cpsatPairingService
        );
        
        threadMXBean = ManagementFactory.getThreadMXBean();
        if (!threadMXBean.isThreadCpuTimeSupported()) {
            System.err.println("WARNING: Thread CPU time measurement is not supported on this JVM");
        }
        threadMXBean.setThreadCpuTimeEnabled(true);
    }

    // ===========================================
    // BYE_ONLY TESTS (Odd player counts: 17, 33, 65, 129)
    // ===========================================

    @Test
    @Order(1)
    void testBacktracking_17Players_Round2_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 2, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(2)
    void testBacktracking_17Players_Round3_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 3, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(3)
    void testBacktracking_17Players_Round4_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 4, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(4)
    void testBacktracking_17Players_Round5_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 5, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(5)
    void testCPSAT_17Players_Round2_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 2, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(6)
    void testCPSAT_17Players_Round3_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 3, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(7)
    void testCPSAT_17Players_Round4_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 4, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(8)
    void testCPSAT_17Players_Round5_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 5, ConstraintScenario.BYE_ONLY);
    }

    // 33 players - BYE_ONLY
    @Test
    @Order(9)
    void testBacktracking_33Players_Round2_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 2, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(10)
    void testBacktracking_33Players_Round3_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 3, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(11)
    void testBacktracking_33Players_Round4_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 4, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(12)
    void testBacktracking_33Players_Round5_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 5, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(13)
    void testCPSAT_33Players_Round2_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 2, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(14)
    void testCPSAT_33Players_Round3_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 3, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(15)
    void testCPSAT_33Players_Round4_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 4, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(16)
    void testCPSAT_33Players_Round5_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 5, ConstraintScenario.BYE_ONLY);
    }

    // 65 players - BYE_ONLY  
    @Test
    @Order(17)
    void testBacktracking_65Players_Round2_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 2, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(18)
    void testBacktracking_65Players_Round3_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 3, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(19)
    void testBacktracking_65Players_Round4_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 4, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(20)
    void testBacktracking_65Players_Round5_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 5, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(21)
    void testCPSAT_65Players_Round2_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 2, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(22)
    void testCPSAT_65Players_Round3_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 3, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(23)
    void testCPSAT_65Players_Round4_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 4, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(24)
    void testCPSAT_65Players_Round5_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 5, ConstraintScenario.BYE_ONLY);
    }

    // 129 players - BYE_ONLY
    @Test
    @Order(25)
    void testBacktracking_129Players_Round2_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 2, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(26)
    void testBacktracking_129Players_Round3_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 3, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(27)
    void testBacktracking_129Players_Round4_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 4, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(28)
    void testBacktracking_129Players_Round5_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 5, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(29)
    void testCPSAT_129Players_Round2_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 2, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(30)
    void testCPSAT_129Players_Round3_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 3, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(31)
    void testCPSAT_129Players_Round4_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 4, ConstraintScenario.BYE_ONLY);
    }

    @Test
    @Order(32)
    void testCPSAT_129Players_Round5_ByeOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 5, ConstraintScenario.BYE_ONLY);
    }

    // ===========================================
    // TEAM_ONLY TESTS (Even player counts: 16, 32, 64, 128)
    // ===========================================

    @Test
    @Order(33)
    void testBacktracking_16Players_Round2_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 16, 2, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(34)
    void testBacktracking_16Players_Round3_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 16, 3, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(35)
    void testBacktracking_16Players_Round4_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 16, 4, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(36)
    void testBacktracking_16Players_Round5_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 16, 5, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(37)
    void testCPSAT_16Players_Round2_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 16, 2, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(38)
    void testCPSAT_16Players_Round3_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 16, 3, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(39)
    void testCPSAT_16Players_Round4_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 16, 4, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(40)
    void testCPSAT_16Players_Round5_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 16, 5, ConstraintScenario.TEAM_ONLY);
    }

    // 32 players - TEAM_ONLY
    @Test
    @Order(41)
    void testBacktracking_32Players_Round2_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 32, 2, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(42)
    void testBacktracking_32Players_Round3_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 32, 3, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(43)
    void testBacktracking_32Players_Round4_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 32, 4, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(44)
    void testBacktracking_32Players_Round5_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 32, 5, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(45)
    void testCPSAT_32Players_Round2_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 32, 2, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(46)
    void testCPSAT_32Players_Round3_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 32, 3, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(47)
    void testCPSAT_32Players_Round4_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 32, 4, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(48)
    void testCPSAT_32Players_Round5_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 32, 5, ConstraintScenario.TEAM_ONLY);
    }

    // 64 players - TEAM_ONLY
    @Test
    @Order(49)
    void testBacktracking_64Players_Round2_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 2, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(50)
    void testBacktracking_64Players_Round3_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 3, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(51)
    void testBacktracking_64Players_Round4_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 4, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(52)
    void testBacktracking_64Players_Round5_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 5, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(53)
    void testCPSAT_64Players_Round2_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 2, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(54)
    void testCPSAT_64Players_Round3_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 3, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(55)
    void testCPSAT_64Players_Round4_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 4, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(56)
    void testCPSAT_64Players_Round5_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 5, ConstraintScenario.TEAM_ONLY);
    }

    // 128 players - TEAM_ONLY
    @Test
    @Order(57)
    void testBacktracking_128Players_Round2_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 2, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(58)
    void testBacktracking_128Players_Round3_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 3, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(59)
    void testBacktracking_128Players_Round4_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 4, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(60)
    void testBacktracking_128Players_Round5_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 5, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(61)
    void testCPSAT_128Players_Round2_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 2, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(62)
    void testCPSAT_128Players_Round3_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 3, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(63)
    void testCPSAT_128Players_Round4_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 4, ConstraintScenario.TEAM_ONLY);
    }

    @Test
    @Order(64)
    void testCPSAT_128Players_Round5_TeamOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 5, ConstraintScenario.TEAM_ONLY);
    }

    // ===========================================
    // CITY_ONLY TESTS (Even player counts: 16, 32, 64, 128)
    // ===========================================

    @Test
    @Order(65)
    void testBacktracking_16Players_Round2_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 16, 2, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(66)
    void testBacktracking_16Players_Round3_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 16, 3, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(67)
    void testBacktracking_16Players_Round4_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 16, 4, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(68)
    void testBacktracking_16Players_Round5_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 16, 5, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(69)
    void testCPSAT_16Players_Round2_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 16, 2, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(70)
    void testCPSAT_16Players_Round3_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 16, 3, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(71)
    void testCPSAT_16Players_Round4_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 16, 4, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(72)
    void testCPSAT_16Players_Round5_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 16, 5, ConstraintScenario.CITY_ONLY);
    }

    // 32 players - CITY_ONLY
    @Test
    @Order(73)
    void testBacktracking_32Players_Round2_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 32, 2, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(74)
    void testBacktracking_32Players_Round3_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 32, 3, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(75)
    void testBacktracking_32Players_Round4_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 32, 4, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(76)
    void testBacktracking_32Players_Round5_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 32, 5, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(77)
    void testCPSAT_32Players_Round2_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 32, 2, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(78)
    void testCPSAT_32Players_Round3_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 32, 3, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(79)
    void testCPSAT_32Players_Round4_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 32, 4, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(80)
    void testCPSAT_32Players_Round5_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 32, 5, ConstraintScenario.CITY_ONLY);
    }

    // 64 players - CITY_ONLY
    @Test
    @Order(81)
    void testBacktracking_64Players_Round2_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 2, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(82)
    void testBacktracking_64Players_Round3_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 3, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(83)
    void testBacktracking_64Players_Round4_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 4, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(84)
    void testBacktracking_64Players_Round5_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 5, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(85)
    void testCPSAT_64Players_Round2_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 2, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(86)
    void testCPSAT_64Players_Round3_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 3, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(87)
    void testCPSAT_64Players_Round4_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 4, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(88)
    void testCPSAT_64Players_Round5_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 5, ConstraintScenario.CITY_ONLY);
    }

    // 128 players - CITY_ONLY
    @Test
    @Order(89)
    void testBacktracking_128Players_Round2_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 2, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(90)
    void testBacktracking_128Players_Round3_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 3, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(91)
    void testBacktracking_128Players_Round4_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 4, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(92)
    void testBacktracking_128Players_Round5_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 5, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(93)
    void testCPSAT_128Players_Round2_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 2, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(94)
    void testCPSAT_128Players_Round3_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 3, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(95)
    void testCPSAT_128Players_Round4_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 4, ConstraintScenario.CITY_ONLY);
    }

    @Test
    @Order(96)
    void testCPSAT_128Players_Round5_CityOnly() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 5, ConstraintScenario.CITY_ONLY);
    }

    // ===========================================
    // BYE_TEAM TESTS (Odd player counts with team constraints: 17, 33, 65, 129)
    // ===========================================

    @Test
    @Order(97)
    void testBacktracking_17Players_Round2_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 2, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(98)
    void testBacktracking_17Players_Round3_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 3, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(99)
    void testBacktracking_17Players_Round4_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 4, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(100)
    void testBacktracking_17Players_Round5_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 5, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(101)
    void testCPSAT_17Players_Round2_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 2, ConstraintScenario.BYE_TEAM);
   }

    @Test
    @Order(102)
    void testCPSAT_17Players_Round3_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 3, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(103)
    void testCPSAT_17Players_Round4_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 4, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(104)
    void testCPSAT_17Players_Round5_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 5, ConstraintScenario.BYE_TEAM);
    }

    // 33 players - BYE_TEAM
    @Test
    @Order(105)
    void testBacktracking_33Players_Round2_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 2, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(106)
    void testBacktracking_33Players_Round3_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 3, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(107)
    void testBacktracking_33Players_Round4_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 4, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(108)
    void testBacktracking_33Players_Round5_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 5, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(109)
    void testCPSAT_33Players_Round2_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 2, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(110)
    void testCPSAT_33Players_Round3_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 3, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(111)
    void testCPSAT_33Players_Round4_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 4, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(112)
    void testCPSAT_33Players_Round5_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 5, ConstraintScenario.BYE_TEAM);
    }

    // 65 players - BYE_TEAM
    @Test
    @Order(113)
    void testBacktracking_65Players_Round2_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 2, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(114)
    void testBacktracking_65Players_Round3_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 3, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(115)
    void testBacktracking_65Players_Round4_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 4, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(116)
    void testBacktracking_65Players_Round5_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 5, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(117)
    void testCPSAT_65Players_Round2_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 2, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(118)
    void testCPSAT_65Players_Round3_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 3, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(119)
    void testCPSAT_65Players_Round4_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 4, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(120)
    void testCPSAT_65Players_Round5_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 5, ConstraintScenario.BYE_TEAM);
    }

    // 129 players - BYE_TEAM
    @Test
    @Order(121)
    void testBacktracking_129Players_Round2_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 2, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(122)
    void testBacktracking_129Players_Round3_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 3, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(123)
    void testBacktracking_129Players_Round4_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 4, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(124)
    void testBacktracking_129Players_Round5_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 5, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(125)
    void testCPSAT_129Players_Round2_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 2, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(126)
    void testCPSAT_129Players_Round3_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 3, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(127)
    void testCPSAT_129Players_Round4_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 4, ConstraintScenario.BYE_TEAM);
    }

    @Test
    @Order(128)
    void testCPSAT_129Players_Round5_ByeTeam() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 5, ConstraintScenario.BYE_TEAM);
    }

    // ===========================================
    // ALL_CONSTRAINTS TESTS (BYE + TEAM + CITY: 17, 33, 65, 129)
    // ===========================================

    @Test
    @Order(129)
    void testBacktracking_17Players_Round2_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 2, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(130)
    void testBacktracking_17Players_Round3_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 3, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(131)
    void testBacktracking_17Players_Round4_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 4, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(132)
    void testBacktracking_17Players_Round5_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 17, 5, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(133)
    void testCPSAT_17Players_Round2_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 2, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(134)
    void testCPSAT_17Players_Round3_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 3, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(135)
    void testCPSAT_17Players_Round4_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 4, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(136)
    void testCPSAT_17Players_Round5_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 17, 5, ConstraintScenario.ALL_CONSTRAINTS);
    }

    // 33 players - ALL_CONSTRAINTS
    @Test
    @Order(137)
    void testBacktracking_33Players_Round2_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 2, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(138)
    void testBacktracking_33Players_Round3_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 3, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(139)
    void testBacktracking_33Players_Round4_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 4, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(140)
    void testBacktracking_33Players_Round5_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 33, 5, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(141)
    void testCPSAT_33Players_Round2_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 2, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(142)
    void testCPSAT_33Players_Round3_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 3, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(143)
    void testCPSAT_33Players_Round4_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 4, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(144)
    void testCPSAT_33Players_Round5_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 33, 5, ConstraintScenario.ALL_CONSTRAINTS);
    }

    // 65 players - ALL_CONSTRAINTS
    @Test
    @Order(145)
    void testBacktracking_65Players_Round2_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 2, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(146)
    void testBacktracking_65Players_Round3_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 3, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(147)
    void testBacktracking_65Players_Round4_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 4, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(148)
    void testBacktracking_65Players_Round5_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 65, 5, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(149)
    void testCPSAT_65Players_Round2_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 2, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(150)
    void testCPSAT_65Players_Round3_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 3, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(151)
    void testCPSAT_65Players_Round4_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 4, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(152)
    void testCPSAT_65Players_Round5_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 65, 5, ConstraintScenario.ALL_CONSTRAINTS);
    }

    // 129 players - ALL_CONSTRAINTS
    @Test
    @Order(153)
    void testBacktracking_129Players_Round2_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 2, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(154)
    void testBacktracking_129Players_Round3_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 3, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(155)
    void testBacktracking_129Players_Round4_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 4, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(156)
    void testBacktracking_129Players_Round5_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 129, 5, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(157)
    void testCPSAT_129Players_Round2_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 2, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(158)
    void testCPSAT_129Players_Round3_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 3, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(159)
    void testCPSAT_129Players_Round4_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 4, ConstraintScenario.ALL_CONSTRAINTS);
    }

    @Test
    @Order(160)
    void testCPSAT_129Players_Round5_AllConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 129, 5, ConstraintScenario.ALL_CONSTRAINTS);
    }

    // ====================================================================================
    // CORE PERFORMANCE TEST EXECUTION
    // ====================================================================================

    private void runPerformanceTest(PairingAlgorithmType algorithm, int playerCount, int roundNumber, ConstraintScenario scenario) {
        System.out.println("\n>>> Testing " + algorithm + " with " + playerCount + " players, Round " + roundNumber + " - " + scenario.description);
        
        // Generate test data
        TournamentTestData testData = generateTournamentData(playerCount, roundNumber, algorithm, scenario);
        
        // Setup mocks
        setupMocks(testData, roundNumber, algorithm);
        
        // Warmup runs
        System.out.print("Warmup: ");
        for (int i = 0; i < WARMUP_RUNS; i++) {
            try {
                pairingService.createNextRoundPairings(1L, roundNumber);
                System.out.print(".");
            } catch (Exception e) {
                System.out.print("x");
            }
        }
        System.out.println();

        // Measurement runs
        List<Long> executionTimes = new ArrayList<>();
        List<Long> cpuTimes = new ArrayList<>();
        boolean allSuccessful = true;

        System.out.print("Measurement: ");
        for (int i = 0; i < MEASUREMENT_RUNS; i++) {
            // Force garbage collection before measurement
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long startTime = System.nanoTime();
            long startCpuTime = threadMXBean.getCurrentThreadCpuTime();
            
            boolean success = false;
            try {
                List<Match> matches = pairingService.createNextRoundPairings(1L, roundNumber);
                
                // Validate results
                validatePairings(matches, testData);
                success = true;
                System.out.print("✓");
            } catch (Exception e) {
                System.out.print("✗");
                allSuccessful = false;
            }
            
            long endCpuTime = threadMXBean.getCurrentThreadCpuTime();
            long endTime = System.nanoTime();

            if (success) {
                executionTimes.add((endTime - startTime) / 1_000_000); // Convert to ms
                cpuTimes.add((endCpuTime - startCpuTime) / 1_000_000); // Convert to ms
            }
        }
        System.out.println();

        // Calculate statistics
        PerformanceResult result = calculateStatistics(
                algorithm.name(), 
                playerCount, 
                roundNumber,
                scenario,
                executionTimes,
                cpuTimes,
                allSuccessful
        );
        
        results.add(result);

        System.out.printf("Result: Avg=%.2fms, Min=%.2fms, Max=%.2fms, CPU=%.2fms%n",
                result.avgExecutionTime, (double) result.minExecutionTime, 
                (double) result.maxExecutionTime, result.avgCpuTime);
    }

    // ====================================================================================
    // DATA GENERATION WITH CONSTRAINT SUPPORT
    // ====================================================================================

    private TournamentTestData generateTournamentData(int playerCount, int roundNumber, PairingAlgorithmType algorithm, ConstraintScenario scenario) {
        TournamentTestData data = new TournamentTestData();
        data.scenario =scenario;
        
        // Create game system
        data.gameSystem = createGameSystem();
        
        // Create teams based on scenario
        int teamCount = scenario.hasTeamConstraint ? Math.max(4, playerCount / 4) : 1;
        data.teams = createTeams(data.gameSystem, teamCount);
        
        // Create users with appropriate city/team distribution
        data.users = createUsers(playerCount, data.teams, scenario);
        
        // Create team members
        data.teamMembers = createTeamMembers(data.users, data.teams, data.gameSystem);
        
        // Create tournament
        data.tournament = createTournament(data.gameSystem, data.users);
        
        // Create round definitions
        data.roundDefinitions = createRoundDefinitions(data.tournament, roundNumber, algorithm);
        
        // Create previous rounds with completed matches
        createPreviousRounds(data.tournament, data.users, roundNumber);
        
        return data;
    }

    private GameSystem createGameSystem() {
        GameSystem gs = new GameSystem();
        gs.setId(1L);
        gs.setName("Test Game System");
        gs.setDefaultRoundNumber(3);
        gs.setPrimaryScoreEnabled(true);
        gs.setSecondaryScoreEnabled(true);
        gs.setThirdScoreEnabled(false);
        gs.setAdditionalScoreEnabled(false);
        return gs;
    }

    private List<Team> createTeams(GameSystem gameSystem, int teamCount) {
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < teamCount; i++) {
            Team team = Team.builder()
                    .id((long) (i + 1))
                    .name("Team " + (i + 1))
                    .abbreviation("T" + (i + 1))
                    .city(CITIES[i % CITIES.length])
                    .gameSystem(gameSystem)
                    .build();
            teams.add(team);
        }
        return teams;
    }

    private List<User> createUsers(int playerCount, List<Team> teams, ConstraintScenario scenario) {
        List<User> users = new ArrayList<>();
        
        for (int i = 0; i < playerCount; i++) {
            User user = new User();
            user.setId((long) (i + 1));
            user.setName("Player" + (i + 1));
            user.setEmail("player" + (i + 1) + "@test.com");
            user.setPassword("password");
            
            // Assign city based on scenario
            if (scenario.hasCityConstraint) {
                // Distribute players across cities
                user.setCity(CITIES[i % CITIES.length]);
            } else {
                // All same city (no city constraint)
                user.setCity(CITIES[0]);
            }
            
            users.add(user);
        }
        
        return users;
    }

    private List<TeamMember> createTeamMembers(List<User> users, List<Team> teams, GameSystem gameSystem) {
        List<TeamMember> teamMembers = new ArrayList<>();
        
        for (int i = 0; i < users.size(); i++) {
            TeamMember member = new TeamMember();
            member.setId((long) (i + 1));
            member.setUser(users.get(i));
            
            // Distribute players across teams
            Team team = teams.get(i % teams.size());
            member.setTeam(team);
            member.setStatus(TeamMemberStatus.ACTIVE);
            member.setJoinedAt(LocalDateTime.now());
            
            teamMembers.add(member);
        }
        
        return teamMembers;
    }

    private Tournament createTournament(GameSystem gameSystem, List<User> users) {
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Constraint Test Tournament");
        tournament.setGameSystem(gameSystem);
        tournament.setNumberOfRounds(5);
        tournament.setRoundDurationMinutes(120);
        tournament.setStatus(com.tourney.dto.tournament.TournamentStatus.ACTIVE);
        tournament.setPhase(TournamentPhase.ROUND_ACTIVE);
        tournament.setType(com.tourney.dto.tournament.TournamentType.SWISS);
        tournament.setStartDate(java.time.LocalDate.now());
        tournament.setRounds(new ArrayList<>());
        
        // Add participants
        for (User user : users) {
            com.tourney.domain.participant.TournamentParticipant participant = new com.tourney.domain.participant.TournamentParticipant();
            participant.setUser(user);
            participant.setTournament(tournament);
            participant.setConfirmed(true);
            tournament.getParticipantLinks().add(participant);
        }
        
        return tournament;
    }

    private List<TournamentRoundDefinition> createRoundDefinitions(Tournament tournament, int maxRound, PairingAlgorithmType algorithm) {
        List<TournamentRoundDefinition> definitions = new ArrayList<>();
        
        for (int i = 1; i <= maxRound; i++) {
            TournamentRoundDefinition def = new TournamentRoundDefinition();
            def.setId((long) i);
            def.setTournament(tournament);
            def.setRoundNumber(i);
            def.setPairingAlgorithm(algorithm);
            def.setTableAssignmentStrategy(TableAssignmentStrategy.BEST_FIRST);
            def.setAvoidSameTeamPairing(true);
            def.setAvoidSameCityPairing(true);
            def.setByeSmallPoints(20);
            def.setByeLargePoints(null);
            definitions.add(def);
            tournament.getRoundDefinitions().add(def);
        }
        
        return definitions;
    }

    private void createPreviousRounds(Tournament tournament, List<User> users, int currentRoundNumber) {
        // Create completed rounds before the current one
        for (int roundNum = 1; roundNum < currentRoundNumber; roundNum++) {
            TournamentRound round = new TournamentRound();
            round.setId((long) roundNum);
            round.setRoundNumber(roundNum);
            round.setTournament(tournament);
            round.setStatus(RoundStatus.COMPLETED);
            
            // Generate random pairings for this round
            List<User> shuffledUsers = new ArrayList<>(users);
            Collections.shuffle(shuffledUsers, RANDOM);
            
            for (int i = 0; i < shuffledUsers.size() - 1; i += 2) {
                TournamentMatch match = createCompletedMatch(
                        shuffledUsers.get(i), 
                        shuffledUsers.get(i + 1),
                        round,
                        (i / 2) + 1
                );
                round.getMatches().add(match);
            }
            
            // Handle BYE if odd number of players
            if (shuffledUsers.size() % 2 != 0) {
                User byePlayer = shuffledUsers.get(shuffledUsers.size() - 1);
                TournamentMatch byeMatch = createByeMatch(byePlayer, round, (shuffledUsers.size() / 2) + 1);
                round.getMatches().add(byeMatch);
            }
            
            tournament.getRounds().add(round);
        }
        
        // Create the current round (empty, ready for pairings)
        TournamentRound currentRound = new TournamentRound();
        currentRound.setId((long) currentRoundNumber);
        currentRound.setRoundNumber(currentRoundNumber);
        currentRound.setTournament(tournament);
        currentRound.setStatus(RoundStatus.IN_PROGRESS);
        tournament.getRounds().add(currentRound);
    }

    private TournamentMatch createCompletedMatch(User player1, User player2, TournamentRound round, int tableNumber) {
        TournamentMatch match = new TournamentMatch();
        match.setId(RANDOM.nextLong(1000000));
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setTournamentRound(round);
        match.setTableNumber(tableNumber);
        match.setCompleted(true);
        match.setStatus(MatchStatus.COMPLETED);
        
        // Create match rounds
        for (int i = 1; i <= 3; i++) {
            MatchRound matchRound = new MatchRound();
            matchRound.setId(RANDOM.nextLong(1000000));
            matchRound.setRoundNumber(i);
            matchRound.setMatch(match);
            match.getRounds().add(matchRound);
        }
        
        // Random winner
        User winner = RANDOM.nextBoolean() ? player1 : player2;
        
        MatchResult result = new MatchResult();
        result.setWinnerId(winner.getId());
        result.setSubmittedById(player1.getId());
        result.setSubmissionTime(LocalDateTime.now());
        
        match.setMatchResult(result);
        
        return match;
    }

    private TournamentMatch createByeMatch(User player, TournamentRound round, int tableNumber) {
        TournamentMatch match = new TournamentMatch();
        match.setId(RANDOM.nextLong(1000000));
        match.setPlayer1(player);
        match.setPlayer2(null); // BYE
        match.setTournamentRound(round);
        match.setTableNumber(tableNumber);
        match.setCompleted(true);
        match.setStatus(MatchStatus.COMPLETED);
        
        // BYE player automatically wins
        MatchResult result = new MatchResult();
        result.setWinnerId(player.getId());
        result.setSubmittedById(player.getId());
        result.setSubmissionTime(LocalDateTime.now());
        
        match.setMatchResult(result);
        
        return match;
    }

    // ====================================================================================
    // MOCK SETUP
    // ====================================================================================

    private void setupMocks(TournamentTestData data, int roundNumber, PairingAlgorithmType algorithm) {
        // Tournament repository
        when(tournamentRepository.findById(anyLong())).thenReturn(Optional.of(data.tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // Match repository
        when(matchRepository.saveAll(any())).thenAnswer(invocation -> {
            List<Match> matches = invocation.getArgument(0);
            // Assign IDs to new matches
            for (int i = 0; i < matches.size(); i++) {
                if (matches.get(i).getId() == null) {
                    matches.get(i).setId((long) (i + 1000));
                }
            }
            return matches;
        });
        
        // Score repository
        when(scoreRepository.findAllByTournamentId(anyLong())).thenReturn(generateScores(data));
        when(scoreRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        
        //  Round definition repository
        when(roundDefinitionRepository.findByTournamentIdAndRoundNumber(anyLong(), any(Integer.class)))
                .thenAnswer(invocation -> {
                    int rn = invocation.getArgument(1);
                    return data.roundDefinitions.stream()
                            .filter(def -> def.getRoundNumber() == rn)
                            .findFirst();
                });
        
        // Team member repository
        when(teamMemberRepository.findActiveMembership(any(User.class), any(GameSystem.class), any(TeamMemberStatus.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    return data.teamMembers.stream()
                            .filter(tm -> tm.getUser().getId().equals(user.getId()))
                            .findFirst();
                });
    }

    private List<com.tourney.domain.scores.Score> generateScores(TournamentTestData data) {
        List<com.tourney.domain.scores.Score> scores = new ArrayList<>();
        
        for (TournamentRound round : data.tournament.getRounds()) {
            if (round.getStatus() == RoundStatus.COMPLETED) {
                for (Match match : round.getMatches()) {
                    // Generate random scores for completed matches
                    for (MatchRound matchRound : match.getRounds()) {
                        // Player 1 scores
                        com.tourney.domain.scores.Score score1 = new com.tourney.domain.scores.Score();
                        score1.setId(RANDOM.nextLong(1000000));
                        score1.setMatchRound(matchRound);
                        score1.setSide(com.tourney.domain.scores.MatchSide.PLAYER1);
                        score1.setScoreType(com.tourney.domain.scores.ScoreType.MAIN_SCORE);
                        score1.setScore((long) (RANDOM.nextInt(21) + 10)); // 10-30 points
                        score1.setUser(match.getPlayer1());
                        scores.add(score1);
                        
                        // Player 2 scores (if not BYE)
                        if (match.getPlayer2() != null) {
                            com.tourney.domain.scores.Score score2 = new com.tourney.domain.scores.Score();
                            score2.setId(RANDOM.nextLong(1000000));
                            score2.setMatchRound(matchRound);
                            score2.setSide(com.tourney.domain.scores.MatchSide.PLAYER2);
                            score2.setScoreType(com.tourney.domain.scores.ScoreType.MAIN_SCORE);
                            score2.setScore((long) (RANDOM.nextInt(21) + 10)); // 10-30 points
                            score2.setUser(match.getPlayer2());
                            scores.add(score2);
                        }
                    }
                }
            }
        }
        
        return scores;
    }

    // ====================================================================================
    // VALIDATION
    // ====================================================================================

    private void validatePairings(List<Match> matches, TournamentTestData data) {
        assertNotNull(matches, "Matches should not be null");
        assertFalse(matches.isEmpty(), "Should generate at least one match");
        
        // Check for duplicate pairings
        Set<String> pairings = new HashSet<>();
        for (Match match : matches) {
            if (match.getPlayer1() != null && match.getPlayer2() != null) {
                long p1 = match.getPlayer1().getId();
                long p2 = match.getPlayer2().getId();
                String key = Math.min(p1, p2) + "-" + Math.max(p1, p2);
                assertFalse(pairings.contains(key), "Duplicate pairing detected: " + key);
                pairings.add(key);
            }
        }
        
        // Check that all players are paired (accounting for possible BYE)
        Set<Long> pairedPlayers = new HashSet<>();
        for (Match match : matches) {
            if (match.getPlayer1() != null) {
                pairedPlayers.add(match.getPlayer1().getId());
            }
            if (match.getPlayer2() != null) {
                pairedPlayers.add(match.getPlayer2().getId());
            }
        }
        
        int expectedPlayers = data.users.size();
        assertEquals(expectedPlayers, pairedPlayers.size(), 
                "All players should be paired");
        
        // Validate no rematches from previous rounds
        Set<String> previousPairings = getPreviousPairings(data.tournament);
        for (Match match : matches) {
            if (match.getPlayer1() != null && match.getPlayer2() != null) {
                long p1 = match.getPlayer1().getId();
                long p2 = match.getPlayer2().getId();
                String key = Math.min(p1, p2) + "-" + Math.max(p1, p2);
                assertFalse(previousPairings.contains(key), 
                        "Rematch detected: " + key);
            }
        }
    }

    private Set<String> getPreviousPairings(Tournament tournament) {
        return tournament.getRounds().stream()
                .filter(round -> round.getStatus() == RoundStatus.COMPLETED)
                .flatMap(round -> round.getMatches().stream())
                .filter(match -> match.getPlayer1() != null && match.getPlayer2() != null)
                .map(match -> {
                    long p1 = match.getPlayer1().getId();
                    long p2 = match.getPlayer2().getId();
                    return Math.min(p1, p2) + "-" + Math.max(p1, p2);
                })
                .collect(Collectors.toSet());
    }

    // ====================================================================================
    // STATISTICS
    // ====================================================================================

    private PerformanceResult calculateStatistics(
            String algorithm, 
            int playerCount, 
            int roundNumber,
            ConstraintScenario scenario,
            List<Long> executionTimes,
            List<Long> cpuTimes,
            boolean success) {
        
        PerformanceResult result = new PerformanceResult();
        result.algorithm = algorithm;
        result.playerCount = playerCount;
        result.roundNumber = roundNumber;
        result.scenario = scenario;
        result.runs = executionTimes.size();
        result.success = success;
        
        if (!executionTimes.isEmpty()) {
            result.avgExecutionTime = executionTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            
            result.minExecutionTime = executionTimes.stream()
                    .mapToLong(Long::longValue)
                    .min()
                    .orElse(0L);
            
            result.maxExecutionTime = executionTimes.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0L);
            
            // Calculate standard deviation
            double avg = result.avgExecutionTime;
            double variance = executionTimes.stream()
                    .mapToDouble(t -> Math.pow(t - avg, 2))
                    .average()
                    .orElse(0.0);
            result.stdDevExecutionTime = Math.sqrt(variance);
        }
        
        if (!cpuTimes.isEmpty()) {
            result.avgCpuTime = cpuTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
        }
        
        return result;
    }

    // ====================================================================================
    // REPORT GENERATION
    // ====================================================================================

    @AfterAll
    static void generateReport() throws IOException {
        System.out.println("\n===========================================");
        System.out.println("CONSTRAINT PERFORMANCE TEST RESULTS");
        System.out.println("===========================================\n");

        // Console output
        System.out.printf("%-15s %-10s %-8s %-20s %-15s %-15s %-15s %-15s %-10s%n",
                "Algorithm", "Players", "Round", "Scenario", "Avg Time (ms)", "Min Time (ms)", 
                "Max Time (ms)", "Avg CPU (ms)", "Success");
        System.out.println("-".repeat(140));

        for (PerformanceResult result : results) {
            System.out.printf("%-15s %-10d %-8d %-20s %-15.2f %-15.2f %-15.2f %-15.2f %-10s%n",
                    result.algorithm,
                    result.playerCount,
                    result.roundNumber,
                    result.scenario.description,
                    result.avgExecutionTime,
                    (double) result.minExecutionTime,
                    (double) result.maxExecutionTime,
                    result.avgCpuTime,
                    result.success ? "✓" : "✗");
        }

        // CSV export
        String csvFile = "pairing_constraint_performance_results.csv";
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Algorithm,Players,Round,Scenario,AvgTime_ms,MinTime_ms,MaxTime_ms,StdDev_ms,AvgCPU_ms,Success,Runs\n");
            
            for (PerformanceResult result : results) {
                writer.write(String.format("%s,%d,%d,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%d%n",
                        result.algorithm,
                        result.playerCount,
                        result.roundNumber,
                        result.scenario.name(),
                        result.avgExecutionTime,
                        (double) result.minExecutionTime,
                        (double) result.maxExecutionTime,
                        result.stdDevExecutionTime,
                        result.avgCpuTime,
                        result.success ? "TRUE" : "FALSE",
                        result.runs));
            }
        }

        System.out.println("\n✓ CSV report generated: " + csvFile);
        System.out.println("===========================================\n");
    }

    // ====================================================================================
    // DATA STRUCTURES
    // ====================================================================================

    @Getter
    private static class TournamentTestData {
        GameSystem gameSystem;
        List<Team> teams;
        List<User> users;
        List<TeamMember> teamMembers;
        Tournament tournament;
        List<TournamentRoundDefinition> roundDefinitions;
        ConstraintScenario scenario;
    }

    private static class PerformanceResult {
        String algorithm;
        int playerCount;
        int roundNumber;
        ConstraintScenario scenario;
        double avgExecutionTime;
        long minExecutionTime;
        long maxExecutionTime;
        double stdDevExecutionTime;
        double avgCpuTime;
        boolean success;
        int runs;
    }
}
