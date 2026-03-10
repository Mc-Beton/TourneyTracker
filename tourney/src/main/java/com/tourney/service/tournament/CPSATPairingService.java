package com.tourney.service.tournament;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.common.domain.User;
import com.tourney.domain.tournament.TournamentRoundDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serwis parowania graczy używający algorytmu CP-SAT (Constraint Programming - Satisfiability)
 * z Google OR-Tools. Pozwala na zdefiniowanie twardych i miękkich ograniczeń z wagami.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CPSATPairingService {

    // Wagi dla ograniczeń miękkich (można dostosować)
    private static final long SAME_TEAM_PENALTY = 1000;
    private static final long SAME_CITY_PENALTY = 500;
    private static final long RANK_DIFFERENCE_WEIGHT = 10;

    /**
     * Metadane gracza dla algorytmu parowania
     */
    public record PlayerMetadata(Long teamId, String city) {}

    /**
     * Para graczy
     */
    public record Pairing(User player1, User player2) {}

    /**
     * Plan parowania zawierający pary i ewentualnego gracza z BYE
     */
    public record PairingPlan(List<Pairing> pairs, User byePlayer) {}

    /**
     * Próbuje znaleźć optymalne parowanie używając CP-SAT
     *
     * @param rankedUsers Lista graczy posortowana według rankingu (win/points)
     * @param previousPairings Zbiór par graczy, którzy już grali ze sobą
     * @param definition Definicja rundy z konfiguracją ograniczeń
     * @param metadata Metadane graczy (drużyny, miasta)
     * @return Optional z planem parowania lub empty jeśli niemożliwe
     */
    public Optional<PairingPlan> findOptimalPairing(
            List<User> rankedUsers,
            Set<String> previousPairings,
            TournamentRoundDefinition definition,
            Map<Long, PlayerMetadata> metadata
    ) {
        if (rankedUsers.isEmpty()) {
            return Optional.of(new PairingPlan(new ArrayList<>(), null));
        }

        // Załaduj bibliotekę OR-Tools
        try {
            Loader.loadNativeLibraries();
        } catch (Exception e) {
            log.error("Failed to load OR-Tools native libraries", e);
            return Optional.empty();
        }

        // Przypadek parzystej liczby graczy
        if (rankedUsers.size() % 2 == 0) {
            Optional<List<Pairing>> pairs = solvePairingProblem(
                    rankedUsers, previousPairings, definition, metadata, null
            );
            return pairs.map(p -> new PairingPlan(p, null));
        }

        // Przypadek nieparzystej liczby graczy - próbujemy różne opcje BYE od końca
        for (int byeIndex = rankedUsers.size() - 1; byeIndex >= 0; byeIndex--) {
            User byePlayer = rankedUsers.get(byeIndex);
            List<User> remainingPlayers = new ArrayList<>(rankedUsers);
            remainingPlayers.remove(byeIndex);

            Optional<List<Pairing>> pairs = solvePairingProblem(
                    remainingPlayers, previousPairings, definition, metadata, byePlayer
            );

            if (pairs.isPresent()) {
                return Optional.of(new PairingPlan(pairs.get(), byePlayer));
            }
        }

        return Optional.empty();
    }

    /**
     * Rozwiązuje problem parowania używając CP-SAT
     */
    private Optional<List<Pairing>> solvePairingProblem(
            List<User> players,
            Set<String> previousPairings,
            TournamentRoundDefinition definition,
            Map<Long, PlayerMetadata> metadata,
            User byePlayer
    ) {
        int n = players.size();
        if (n == 0) {
            return Optional.of(new ArrayList<>());
        }

        CpModel model = new CpModel();

        // Tworzenie zmiennych decyzyjnych x[i][j] - czy gracz i gra z graczem j
        IntVar[][] x = new IntVar[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                x[i][j] = model.newBoolVar("x_" + i + "_" + j);
            }
        }

        // TWARDE OGRANICZENIA

        // 1. Gracz nie może grać sam ze sobą
        for (int i = 0; i < n; i++) {
            model.addEquality(x[i][i], 0);
        }

        // 2. Każdy gracz gra dokładnie z jednym przeciwnikiem
        for (int i = 0; i < n; i++) {
            LinearExprBuilder sum = LinearExpr.newBuilder();
            for (int j = 0; j < n; j++) {
                sum.add(x[i][j]);
            }
            model.addEquality(sum, 1);
        }

        // 3. Symetria - jeśli i gra z j, to j gra z i
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                model.addEquality(x[i][j], x[j][i]);
            }
        }

        // 4. No rematches - jeśli gracze już grali, nie mogą grać ponownie
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                String pairKey = createPairingKey(players.get(i).getId(), players.get(j).getId());
                if (previousPairings.contains(pairKey)) {
                    model.addEquality(x[i][j], 0);
                }
            }
        }

        // MIĘKKIE OGRANICZENIA (przez funkcję celu)

        List<IntVar> penaltyVars = new ArrayList<>();
        List<Long> penaltyWeights = new ArrayList<>();

        // Kary za te same drużyny
        if (definition.getAvoidSameTeamPairing() != null && definition.getAvoidSameTeamPairing()) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    PlayerMetadata meta1 = metadata.get(players.get(i).getId());
                    PlayerMetadata meta2 = metadata.get(players.get(j).getId());

                    if (meta1.teamId() != null && meta2.teamId() != null &&
                            meta1.teamId().equals(meta2.teamId())) {
                        penaltyVars.add(x[i][j]);
                        penaltyWeights.add(SAME_TEAM_PENALTY);
                    }
                }
            }
        }

        // Kary za te same miasta
        if (definition.getAvoidSameCityPairing() != null && definition.getAvoidSameCityPairing()) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    PlayerMetadata meta1 = metadata.get(players.get(i).getId());
                    PlayerMetadata meta2 = metadata.get(players.get(j).getId());

                    if (meta1.city() != null && meta2.city() != null &&
                            !meta1.city().isEmpty() && !meta2.city().isEmpty() &&
                            meta1.city().equalsIgnoreCase(meta2.city())) {
                        penaltyVars.add(x[i][j]);
                        penaltyWeights.add(SAME_CITY_PENALTY);
                    }
                }
            }
        }

        // Preferuj parowanie graczy o zbliżonym rankingu
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int rankDiff = Math.abs(i - j);
                if (rankDiff > 1) { // Jeśli różnica w rankingu > 1
                    penaltyVars.add(x[i][j]);
                    penaltyWeights.add((long) rankDiff * RANK_DIFFERENCE_WEIGHT);
                }
            }
        }

        // Funkcja celu: minimalizuj sumę kar
        if (!penaltyVars.isEmpty()) {
            LinearExprBuilder objective = LinearExpr.newBuilder();
            for (int i = 0; i < penaltyVars.size(); i++) {
                objective.addTerm(penaltyVars.get(i), penaltyWeights.get(i));
            }
            model.minimize(objective);
        }

        // Rozwiązywanie modelu
        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(10.0); // Timeout 10 sekund
        
        CpSolverStatus status = solver.solve(model);

        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            List<Pairing> pairs = extractPairings(players, x, n, solver);
            
            double objectiveValue = solver.objectiveValue();
            log.info("CP-SAT pairing found with objective value: {} (status: {})", objectiveValue, status);
            
            return Optional.of(pairs);
        } else {
            log.warn("CP-SAT solver failed to find pairing. Status: {}", status);
            return Optional.empty();
        }
    }

    /**
     * Wyciąga pary z rozwiązania solvera
     */
    private List<Pairing> extractPairings(List<User> players, IntVar[][] x, int n, CpSolver solver) {
        List<Pairing> pairs = new ArrayList<>();
        Set<Integer> paired = new HashSet<>();

        for (int i = 0; i < n; i++) {
            if (paired.contains(i)) {
                continue;
            }

            for (int j = i + 1; j < n; j++) {
                if (paired.contains(j)) {
                    continue;
                }

                if (solver.value(x[i][j]) == 1) {
                    pairs.add(new Pairing(players.get(i), players.get(j)));
                    paired.add(i);
                    paired.add(j);
                    break;
                }
            }
        }

        return pairs;
    }

    /**
     * Tworzy klucz pary graczy (mniejsze ID - większe ID)
     */
    private String createPairingKey(Long player1Id, Long player2Id) {
        return Math.min(player1Id, player2Id) + "-" + Math.max(player1Id, player2Id);
    }
}
