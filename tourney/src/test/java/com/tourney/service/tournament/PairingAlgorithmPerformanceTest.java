package com.tourney.service.tournament;

import com.common.domain.User;
import com.tourney.domain.games.*;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.player.PlayerStats;
import com.tourney.domain.scores.*;
import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.team.Team;
import com.tourney.domain.team.TeamMember;
import com.tourney.domain.team.TeamMemberStatus;
import com.tourney.domain.tournament.*;
import com.tourney.dto.tournament.TournamentStatus;
import com.tourney.dto.tournament.TournamentType;
import com.tourney.repository.TournamentRoundDefinitionRepository;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.team.TeamMemberRepository;
import com.tourney.repository.tournament.TournamentRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Comprehensive performance test suite for tournament pairing algorithms.
 * 
 * Tests:
 * - 2 Algorithms: CP-SAT vs Backtracking
 * - 3 Sizes: 16, 32, 64 participants
 * - 2 Rounds: Round 2 and Round 3
 * 
 * Metrics:
 * - Execution Time (milliseconds)
 * - CPU Time (milliseconds)
 * 
 * Generates CSV report for Excel analysis.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PairingAlgorithmPerformanceTest {

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

    // =========================
    // TEST SCENARIOS - 16 PLAYERS
    // =========================

    // =========================
    // BASELINE TESTS (NO CONSTRAINTS)
    // =========================
    
    @Test
    @Order(1)
    void testBacktracking_16Players_Round2_NoConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 16, 2);
    }

    @Test
    @Order(2)
    void testBacktracking_16Players_Round3_NoConstraints() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 16, 3);
    }

    @Test
    @Order(3)
    void testCPSAT_16Players_Round2_NoConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 16, 2);
    }

    @Test
    @Order(4)
    void testCPSAT_16Players_Round3_NoConstraints() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 16, 3);
    }

    // =========================
    // TEST SCENARIOS - 32 PLAYERS
    // =========================

    @Test
    @Order(5)
    void testBacktracking_32Players_Round2() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 32, 2);
    }

    @Test
    @Order(6)
    void testBacktracking_32Players_Round3() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 32, 3);
    }

    @Test
    @Order(7)
    void testCPSAT_32Players_Round2() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 32, 2);
    }

    @Test
    @Order(8)
    void testCPSAT_32Players_Round3() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 32, 3);
    }

    // =========================
    // TEST SCENARIOS - 64 PLAYERS
    // =========================

    @Test
    @Order(9)
    void testBacktracking_64Players_Round2() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 2);
    }

    @Test
    @Order(10)
    void testBacktracking_64Players_Round3() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 3);
    }

    @Test
    @Order(11)
    void testCPSAT_64Players_Round2() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 2);
    }

    @Test
    @Order(12)
    void testCPSAT_64Players_Round3() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 3);
    }

    // 64 players - Rounds 4 and 5
    @Test
    @Order(13)
    void testBacktracking_64Players_Round4() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 4);
    }

    @Test
    @Order(14)
    void testBacktracking_64Players_Round5() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 64, 5);
    }

    @Test
    @Order(15)
    void testCPSAT_64Players_Round4() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 4);
    }

    @Test
    @Order(16)
    void testCPSAT_64Players_Round5() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 64, 5);
    }

    // =========================
    // 128 PLAYERS TESTS
    // =========================

    @Test
    @Order(17)
    void testBacktracking_128Players_Round2() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 2);
    }

    @Test
    @Order(18)
    void testBacktracking_128Players_Round3() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 3);
    }

    @Test
    @Order(19)
    void testBacktracking_128Players_Round4() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 4);
    }

    @Test
    @Order(20)
    void testBacktracking_128Players_Round5() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 128, 5);
    }

    @Test
    @Order(21)
    void testCPSAT_128Players_Round2() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 2);
    }

    @Test
    @Order(22)
    void testCPSAT_128Players_Round3() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 3);
    }

    @Test
    @Order(23)
    void testCPSAT_128Players_Round4() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 4);
    }

    @Test
    @Order(24)
    void testCPSAT_128Players_Round5() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 128, 5);
    }

    // =========================
    // 256 PLAYERS TESTS
    // =========================

    @Test
    @Order(25)
    void testBacktracking_256Players_Round2() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 256, 2);
    }

    @Test
    @Order(26)
    void testBacktracking_256Players_Round3() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 256, 3);
    }

    @Test
    @Order(27)
    void testBacktracking_256Players_Round4() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 256, 4);
    }

    @Test
    @Order(28)
    void testBacktracking_256Players_Round5() {
        runPerformanceTest(PairingAlgorithmType.BACKTRACKING, 256, 5);
    }

    @Test
    @Order(29)
    void testCPSAT_256Players_Round2() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 256, 2);
    }

    @Test
    @Order(30)
    void testCPSAT_256Players_Round3() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 256, 3);
    }

    @Test
    @Order(31)
    void testCPSAT_256Players_Round4() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 256, 4);
    }

    @Test
    @Order(32)
    void testCPSAT_256Players_Round5() {
        runPerformanceTest(PairingAlgorithmType.CP_SAT, 256, 5);
    }

    // =========================
    // GENERATE REPORT
    // =========================

    @AfterAll
    static void generateReport() throws IOException {
        System.out.println("\n===========================================");
        System.out.println("PERFORMANCE TEST RESULTS");
        System.out.println("===========================================\n");

        // Console output
        System.out.printf("%-15s %-10s %-8s %-15s %-15s %-15s %-15s %-10s%n",
                "Algorithm", "Players", "Round", "Avg Time (ms)", "Min Time (ms)", 
                "Max Time (ms)", "Avg CPU (ms)", "Success");
        System.out.println("-".repeat(120));

        for (PerformanceResult result : results) {
            System.out.printf("%-15s %-10d %-8d %-15.2f %-15.2f %-15.2f %-15.2f %-10s%n",
                    result.algorithm,
                    result.playerCount,
                    result.roundNumber,
                    result.avgExecutionTime,
                    (double) result.minExecutionTime,
                    (double) result.maxExecutionTime,
                    result.avgCpuTime,
                    result.success ? "✓" : "✗");
        }

        // CSV export
        String csvFile = "pairing_performance_results.csv";
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("Algorithm,Players,Round,AvgTime_ms,MinTime_ms,MaxTime_ms,StdDev_ms,AvgCPU_ms,Success,Runs\n");
            
            for (PerformanceResult result : results) {
                writer.write(String.format("%s,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%d%n",
                        result.algorithm,
                        result.playerCount,
                        result.roundNumber,
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

    // =========================
    // CORE TEST RUNNER
    // =========================

    private void runPerformanceTest(PairingAlgorithmType algorithm, int playerCount, int roundNumber) {
        System.out.printf("\n>>> Testing %s with %d players, Round %d%n", 
                algorithm, playerCount, roundNumber);

        // Generate test data
        TournamentTestData testData = generateTournamentData(playerCount, roundNumber, algorithm);
        
        // Setup mocks
        setupMocks(testData);

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
                executionTimes,
                cpuTimes,
                allSuccessful
        );
        
        results.add(result);

        System.out.printf("Result: Avg=%.2fms, Min=%.2fms, Max=%.2fms, CPU=%.2fms%n",
                result.avgExecutionTime, (double) result.minExecutionTime, 
                (double) result.maxExecutionTime, result.avgCpuTime);
    }

    // =========================
    // DATA GENERATION
    // =========================

    private TournamentTestData generateTournamentData(int playerCount, int roundNumber, PairingAlgorithmType algorithm) {
        TournamentTestData data = new TournamentTestData();
        
        // Create game system
        data.gameSystem = createGameSystem();
        
        // Create teams
        data.teams = createTeams(data.gameSystem);
        
        // Create users with random team and city assignments
        data.users = createUsers(playerCount, data.teams);
        
        // Create team members
        data.teamMembers = createTeamMembers(data.users, data.teams, data.gameSystem);
        
        // Create tournament
        data.tournament = createTournament(data.gameSystem, data.users, roundNumber);
        
        // Create round definitions
        data.roundDefinitions = createRoundDefinitions(data.tournament, roundNumber, algorithm);
        
        // Create previous rounds with matches and results
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

    private List<Team> createTeams(GameSystem gameSystem) {
        List<Team> teams = new ArrayList<>();
        
        Team teamA = Team.builder()
                .id(1L)
                .name("Team A")
                .abbreviation("TMA")
                .city("City A")
                .gameSystem(gameSystem)
                .build();
        
        Team teamB = Team.builder()
                .id(2L)
                .name("Team B")
                .abbreviation("TMB")
                .city("City B")
                .gameSystem(gameSystem)
                .build();
        
        teams.add(teamA);
        teams.add(teamB);
        
        return teams;
    }

    private List<User> createUsers(int count, List<Team> teams) {
        List<User> users = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId((long) (i + 1));
            user.setName("Player" + (i + 1));
            user.setEmail("player" + (i + 1) + "@test.com");
            user.setPassword("password");
            user.setCity(CITIES[RANDOM.nextInt(CITIES.length)]);
            
            users.add(user);
        }
        
        return users;
    }

    private List<TeamMember> createTeamMembers(List<User> users, List<Team> teams, GameSystem gameSystem) {
        List<TeamMember> members = new ArrayList<>();
        
        for (User user : users) {
            TeamMember member = new TeamMember();
            member.setId((long) user.getId());
            member.setUser(user);
            // Randomly assign to Team A or Team B
            member.setTeam(teams.get(RANDOM.nextInt(teams.size())));
            member.setStatus(TeamMemberStatus.ACTIVE);
            member.setJoinedAt(LocalDateTime.now());
            
            members.add(member);
        }
        
        return members;
    }

    private Tournament createTournament(GameSystem gameSystem, List<User> users, int currentRoundNumber) {
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Performance Test Tournament");
        tournament.setGameSystem(gameSystem);
        tournament.setNumberOfRounds(5);
        tournament.setRoundDurationMinutes(120);
        tournament.setStatus(TournamentStatus.ACTIVE);
        tournament.setPhase(TournamentPhase.ROUND_ACTIVE);
        tournament.setType(TournamentType.SWISS);
        tournament.setCurrentRound(currentRoundNumber - 1); // Previous round completed
        tournament.setStartDate(LocalDate.now());
        
        // Add participants
        for (User user : users) {
            TournamentParticipant participant = new TournamentParticipant();
            participant.setUser(user);
            participant.setTournament(tournament);
            participant.setConfirmed(true);
            tournament.getParticipantLinks().add(participant);
        }
        
        return tournament;
    }

    private List<TournamentRoundDefinition> createRoundDefinitions(
            Tournament tournament, int upToRound, PairingAlgorithmType algorithm) {
        List<TournamentRoundDefinition> definitions = new ArrayList<>();
        
        for (int i = 1; i <= upToRound; i++) {
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
        
        MatchResult result = new MatchResult();
        result.setWinnerId(player.getId());
        result.setSubmittedById(player.getId());
        result.setSubmissionTime(LocalDateTime.now());
        
        match.setMatchResult(result);
        
        return match;
    }

    // =========================
    // MOCK SETUP
    // =========================

    private void setupMocks(TournamentTestData data) {
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
        when(scoreRepository.save(any(Score.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // Round definition repository
        when(roundDefinitionRepository.findByTournamentIdAndRoundNumber(anyLong(), any(Integer.class)))
                .thenAnswer(invocation -> {
                    int roundNumber = invocation.getArgument(1);
                    return data.roundDefinitions.stream()
                            .filter(def -> def.getRoundNumber() == roundNumber)
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

    private List<Score> generateScores(TournamentTestData data) {
        List<Score> scores = new ArrayList<>();
        
        for (TournamentRound round : data.tournament.getRounds()) {
            if (round.getStatus() == RoundStatus.COMPLETED) {
                for (Match match : round.getMatches()) {
                    // Generate random scores for completed matches
                    for (MatchRound matchRound : match.getRounds()) {
                        // Player 1 scores
                        Score score1 = new Score();
                        score1.setId(RANDOM.nextLong(1000000));
                        score1.setMatchRound(matchRound);
                        score1.setSide(MatchSide.PLAYER1);
                        score1.setScoreType(ScoreType.MAIN_SCORE);
                        score1.setScore((long) (RANDOM.nextInt(21) + 10)); // 10-30 points
                        score1.setUser(match.getPlayer1());
                        scores.add(score1);
                        
                        // Player 2 scores (if not BYE)
                        if (match.getPlayer2() != null) {
                            Score score2 = new Score();
                            score2.setId(RANDOM.nextLong(1000000));
                            score2.setMatchRound(matchRound);
                            score2.setSide(MatchSide.PLAYER2);
                            score2.setScoreType(ScoreType.MAIN_SCORE);
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

    // =========================
    // VALIDATION
    // =========================

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

    // =========================
    // STATISTICS
    // =========================

    private PerformanceResult calculateStatistics(
            String algorithm, 
            int playerCount, 
            int roundNumber,
            List<Long> executionTimes,
            List<Long> cpuTimes,
            boolean success) {
        
        PerformanceResult result = new PerformanceResult();
        result.algorithm = algorithm;
        result.playerCount = playerCount;
        result.roundNumber = roundNumber;
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
            double mean = result.avgExecutionTime;
            double variance = executionTimes.stream()
                    .mapToDouble(time -> Math.pow(time - mean, 2))
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

    // =========================
    // DATA STRUCTURES
    // =========================

    private static class TournamentTestData {
        GameSystem gameSystem;
        List<Team> teams;
        List<User> users;
        List<TeamMember> teamMembers;
        Tournament tournament;
        List<TournamentRoundDefinition> roundDefinitions;
    }

    private static class PerformanceResult {
        String algorithm;
        int playerCount;
        int roundNumber;
        double avgExecutionTime;
        long minExecutionTime;
        long maxExecutionTime;
        double stdDevExecutionTime;
        double avgCpuTime;
        boolean success;
        int runs;
    }
}
