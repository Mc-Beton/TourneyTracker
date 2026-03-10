package com.tourney.service.tournament;

import com.tourney.domain.games.*;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.player.PlayerStats;
import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.scores.MatchSide;
import com.tourney.domain.tournament.*;
import com.common.domain.User;
import com.tourney.repository.TournamentRoundDefinitionRepository;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.team.TeamMemberRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.domain.team.TeamMember;
import com.tourney.domain.team.TeamMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubsequentRoundPairingService {
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final ScoreRepository scoreRepository;
    private final TournamentRoundDefinitionRepository roundDefinitionRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CPSATPairingService cpsatPairingService;

    public List<Match> createNextRoundPairings(Long tournamentId, int roundNumber) {
        Tournament tournament = getTournament(tournamentId);
        TournamentRound currentRound = findTournamentRound(tournament, roundNumber);
        
        // Pobierz definicję rundy
        TournamentRoundDefinition roundDefinition = roundDefinitionRepository
                .findByTournamentIdAndRoundNumber(tournamentId, roundNumber)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono definicji rundy " + roundNumber));
        
        List<PlayerStats> rankedPlayers = getRankedPlayers(tournament);
        Set<String> previousPairings = getPreviousPairings(tournament);
        
        // Pobierz dane o drużynach i miastach
        Map<Long, PlayerMetadata> playerMetadata = loadPlayerMetadata(rankedPlayers, tournament);

        // Dobór algorytmu parowania na podstawie definicji rundy
        PairingAlgorithmType algorithmType = roundDefinition.getPairingAlgorithm();
        TableAssignmentStrategy tableStrategy = roundDefinition.getTableAssignmentStrategy();
        
        List<Match> newMatches;
        
        if (algorithmType == PairingAlgorithmType.CP_SAT) {
            // Algorytm CP-SAT - używa solvera optymalizacyjnego
            newMatches = createMatchesWithCPSAT(rankedPlayers, previousPairings, currentRound, tableStrategy, roundDefinition, playerMetadata);
        } else if (algorithmType == PairingAlgorithmType.BACKTRACKING) {
            // Backtracking algorytm - stosujemy systematyczne przeszukiwanie z wybranymi strategiami
            newMatches = createMatches(rankedPlayers, previousPairings, currentRound, tableStrategy, roundDefinition, playerMetadata);
        } else {
            // STANDARD - domyślna strategia (BEST_FIRST)
            newMatches = createMatches(rankedPlayers, previousPairings, currentRound, TableAssignmentStrategy.BEST_FIRST, roundDefinition, playerMetadata);
        }
        
        // Zmiana fazy turnieju - pary dobrane, czeka na start
        tournament.setPhase(TournamentPhase.PAIRINGS_READY);
        tournamentRepository.save(tournament);
        
        List<Match> savedMatches = matchRepository.saveAll(newMatches);
        
        // Zapisz punkty za BYE do repozytorium Score, aby były wliczane do statystyk
        saveByeScores(savedMatches);
        
        return savedMatches;
    }

    private void saveByeScores(List<Match> matches) {
        for (Match match : matches) {
            // Sprawdź czy to BYE (tylko gracz 1, brak gracza 2, i posiada wynik)
            if (match.getPlayer1() != null && match.getPlayer2() == null && match.getMatchResult() != null) {
                processByeMatchScores(match);
            }
        }
    }

    private void processByeMatchScores(Match match) {
        MatchResult result = match.getMatchResult();
        Long playerId = match.getPlayer1().getId();
        PlayerScore playerScore = result.getPlayerResult(playerId);

        if (playerScore == null) return;

        // Pobierz rundy meczu
        List<MatchRound> matchRounds = match.getRounds();
        List<RoundScore> roundScores = playerScore.getRoundScores();

        // Zakładamy, że kolejność i liczba się zgadzają (bo tak tworzy createByeMatch)
        for (int i = 0; i < Math.min(matchRounds.size(), roundScores.size()); i++) {
            MatchRound matchRound = matchRounds.get(i);
            RoundScore roundScore = roundScores.get(i);

            for (Map.Entry<ScoreType, Double> entry : roundScore.getScores().entrySet()) {
                Score score = new Score();
                score.setMatchRound(matchRound);
                score.setSide(MatchSide.PLAYER1);
                score.setScoreType(entry.getKey());
                // Konwersja Double na Long
                score.setScore(entry.getValue().longValue());
                score.setUser(match.getPlayer1());
                score.setEnteredAt(LocalDateTime.now());
                score.setEnteredByUserId(playerId);

                scoreRepository.save(score);
            }
        }
    }

    private Tournament getTournament(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));
    }

    private List<PlayerStats> getRankedPlayers(Tournament tournament) {
        // Pobierz statystyki graczy
        List<PlayerStats> playerStats = calculatePlayerStats(tournament);

        // Sortuj graczy według wyników (najpierw wygrane, potem punkty)
        playerStats.sort((p1, p2) -> {
            if (p1.getWins() != p2.getWins()) {
                return Integer.compare(p2.getWins(), p1.getWins());
            }
            return Long.compare(p2.getTotalPoints(), p1.getTotalPoints());
        });

        return playerStats;
    }

    private Set<String> getPreviousPairings(Tournament tournament) {
        return tournament.getRounds().stream()
                .flatMap(round -> round.getMatches().stream())
                .map(match -> {
                    if (match.getPlayer1() == null || match.getPlayer2() == null) {
                        return null;
                    }
                    return createPairingKey(match.getPlayer1().getId(), match.getPlayer2().getId());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private String createPairingKey(Long player1Id, Long player2Id) {
        return Math.min(player1Id, player2Id) + "-" + Math.max(player1Id, player2Id);
    }

    private boolean havePlayed(Set<String> previousPairings, Long player1Id, Long player2Id) {
        return previousPairings.contains(createPairingKey(player1Id, player2Id));
    }

    private List<PlayerStats> calculatePlayerStats(Tournament tournament) {
        Map<Long, PlayerStats> statsMap = new HashMap<>();

        tournament.getParticipantLinks().stream()
                .filter(TournamentParticipant::isConfirmed)
                .map(TournamentParticipant::getUser)
                .forEach(user -> statsMap.put(user.getId(), new PlayerStats(user)));

        tournament.getRounds().stream()
                .flatMap(round -> round.getMatches().stream())
                .forEach(match -> {
                    if (match.getMatchResult() != null) {
                        Long winnerId = match.getMatchResult().getWinnerId();
                        if (winnerId != null) {
                            PlayerStats winnerStats = statsMap.get(winnerId);
                            if (winnerStats != null) { // np. gdy wygrał ktoś niepotwierdzony / usunięty
                                winnerStats.incrementWins();
                            }
                        }
                    }
                });

        // Dodawanie punktów
        scoreRepository.findAllByTournamentId(tournament.getId())
                .forEach(score -> {
                    PlayerStats stats = statsMap.get(score.getUser().getId());
                    if (stats != null) {
                        stats.addPoints(score.getScore());
                    }
                });

        return new ArrayList<>(statsMap.values());
    }

    private TournamentRound findTournamentRound(Tournament tournament, int roundNumber) {
        return tournament.getRounds().stream()
                .filter(round -> round.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nie znaleziono rundy " + roundNumber));
    }

    private record PlayerMetadata(Long teamId, String city) {}

    private Map<Long, PlayerMetadata> loadPlayerMetadata(List<PlayerStats> rankedPlayers, Tournament tournament) {
        Map<Long, PlayerMetadata> metadata = new HashMap<>();
        List<User> players = rankedPlayers.stream().map(PlayerStats::getUser).toList();
        
        // Pobierz wszystkich członków drużyn dla tych graczy i tego game system
        List<TeamMember> teamMembers = teamMemberRepository.findAll(); 
        // Powyższe jest nieefektywne na dużej skali, ale wystarczy tu. 
        // Lepiej: findByUserIdInAndTeam_GameSystemId(userIds, gameSystemId)
        // Zakładam, że teamMemberRepository.findByUser(user) zwróci listę, a my pofiltrujemy po gameSystemie turnieju.
        
        for (User player : players) {
            Long teamId = null;
            String city = player.getCity(); // Z User entity
            
            Optional<TeamMember> member = teamMemberRepository.findActiveMembership(
                    player, 
                    tournament.getGameSystem(), 
                    TeamMemberStatus.ACTIVE
            );
            
            if (member.isPresent()) {
                teamId = member.get().getTeam().getId();
                // Jeśli miasto w User jest puste, można by wziąć z Teamu? user.getTeam() ?
                if (city == null || city.isEmpty()) {
                     city = member.get().getTeam().getCity();
                }
            }
            
            metadata.put(player.getId(), new PlayerMetadata(teamId, city));
        }
        return metadata;
    }

    private List<Match> createMatches(
            List<PlayerStats> rankedPlayers, 
            Set<String> previousPairings, 
            TournamentRound currentRound,
            TableAssignmentStrategy tableStrategy,
            TournamentRoundDefinition definition,
            Map<Long, PlayerMetadata> playerMetadata
    ) {
        List<Match> matches = new ArrayList<>();
        int tableNumber = 1;

        List<User> rankedUsers = rankedPlayers.stream()
                .map(PlayerStats::getUser)
                .toList();

        // Najpierw próbujemy znaleźć pełne parowanie bez re-matchy (backtracking) Z UWZGLĘDNIENIEM OGRANICZEŃ
        Optional<PairingPlan> noRematchPlan = tryBuildNoRematchPlan(rankedUsers, previousPairings, definition, playerMetadata);
        if (noRematchPlan.isPresent()) {
            PairingPlan plan = noRematchPlan.get();
            for (Pairing pair : plan.pairs()) {
                matches.add(createMatch(pair.player1(), pair.player2(), currentRound, tableNumber));
                tableNumber++;
            }
            if (plan.byePlayer() != null) {
                matches.add(createByeMatch(plan.byePlayer(), currentRound, tableNumber));
            }
        } else {
            // Fallback: zachowujemy dotychczasową strategię, jeśli pełne parowanie bez powtórek jest niemożliwe.
            Set<Long> pairedPlayers = new HashSet<>();

            for (int i = 0; i < rankedPlayers.size(); i++) {
                if (pairedPlayers.contains(rankedPlayers.get(i).getUser().getId())) {
                    continue;
                }

                Optional<Match> match = tryCreateMatch(rankedPlayers, i, pairedPlayers, previousPairings, currentRound, tableNumber);
                if (match.isPresent()) {
                    matches.add(match.get());
                    tableNumber++;
                }
            }

            handleOddNumberOfPlayers(rankedPlayers, pairedPlayers, matches, currentRound, tableNumber);
        }

        // Zastosowanie strategii przypisywania stołów
        if (tableStrategy == TableAssignmentStrategy.RANDOM) {
            assignRandomTableNumbers(matches);
        }
        // Dla BEST_FIRST nic nie robimy - numery stołów są już przypisane sekwencyjnie

        return matches;
    }

    /**
     * Tworzy mecze używając algorytmu CP-SAT
     */
    private List<Match> createMatchesWithCPSAT(
            List<PlayerStats> rankedPlayers,
            Set<String> previousPairings,
            TournamentRound currentRound,
            TableAssignmentStrategy tableStrategy,
            TournamentRoundDefinition definition,
            Map<Long, PlayerMetadata> playerMetadata
    ) {
        List<Match> matches = new ArrayList<>();
        int tableNumber = 1;

        List<User> rankedUsers = rankedPlayers.stream()
                .map(PlayerStats::getUser)
                .toList();

        // Convert metadata to CPSATPairingService format
        Map<Long, CPSATPairingService.PlayerMetadata> cpsatMetadata = playerMetadata.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new CPSATPairingService.PlayerMetadata(e.getValue().teamId(), e.getValue().city())
                ));

        // Użyj CP-SAT do znalezienia optymalnego parowania
        Optional<CPSATPairingService.PairingPlan> pairingPlan = cpsatPairingService.findOptimalPairing(
                rankedUsers, previousPairings, definition, cpsatMetadata
        );

        if (pairingPlan.isPresent()) {
            CPSATPairingService.PairingPlan plan = pairingPlan.get();
            
            // Twórz mecze z par
            for (CPSATPairingService.Pairing pair : plan.pairs()) {
                matches.add(createMatch(pair.player1(), pair.player2(), currentRound, tableNumber));
                tableNumber++;
            }
            
            // Dodaj mecz BYE jeśli jest gracz z BYE
            if (plan.byePlayer() != null) {
                matches.add(createByeMatch(plan.byePlayer(), currentRound, tableNumber));
            }
        } else {
            // Fallback do istniejącego algorytmu jeśli CP-SAT nie znalazł rozwiązania
            return createMatches(rankedPlayers, previousPairings, currentRound, tableStrategy, definition, playerMetadata);
        }

        // Zastosowanie strategii przypisywania stołów
        if (tableStrategy == TableAssignmentStrategy.RANDOM) {
            assignRandomTableNumbers(matches);
        }

        return matches;
    }

    private Optional<PairingPlan> tryBuildNoRematchPlan(
            List<User> rankedUsers, 
            Set<String> previousPairings,
            TournamentRoundDefinition definition,
            Map<Long, PlayerMetadata> metadata
    ) {
        if (rankedUsers.isEmpty()) {
            return Optional.of(new PairingPlan(new ArrayList<>(), null));
        }

        // Parzysta liczba graczy
        if (rankedUsers.size() % 2 == 0) {
            List<Pairing> pairs = new ArrayList<>();
            if (buildPairsWithConstraints(rankedUsers, previousPairings, pairs, definition, metadata)) {
                return Optional.of(new PairingPlan(pairs, null));
            }
            return Optional.empty();
        }

        // Nieparzysta liczba graczy: BYE od końca
        for (int byeIndex = rankedUsers.size() - 1; byeIndex >= 0; byeIndex--) {
            User byePlayer = rankedUsers.get(byeIndex);
            List<User> remainingPlayers = new ArrayList<>(rankedUsers);
            remainingPlayers.remove(byeIndex);

            List<Pairing> pairs = new ArrayList<>();
            if (buildPairsWithConstraints(remainingPlayers, previousPairings, pairs, definition, metadata)) {
                return Optional.of(new PairingPlan(pairs, byePlayer));
            }
        }

        return Optional.empty();
    }
    
    /**
     * Główna logika parowania z Backtrackingiem.
     * Uwzględnia:
     * 1. No Rematch (Hard Constraint - jeśli w previousPairings, to skip)
     * 2. Score Priority (implicit via rankedUsers order)
     * 3. Avoid Team/City (Soft Constraint via reordering candidates)
     */
    private boolean buildPairsWithConstraints(
            List<User> remainingPlayers,
            Set<String> previousPairings,
            List<Pairing> resultPairs,
            TournamentRoundDefinition definition,
            Map<Long, PlayerMetadata> metadata
    ) {
        if (remainingPlayers.isEmpty()) {
            return true;
        }

        User currentPlayer = remainingPlayers.get(0);
        PlayerMetadata currentMeta = metadata.get(currentPlayer.getId());

        // Przygotuj listę kandydatów (indeksy)
        List<Integer> opponentIndices = new ArrayList<>();
        for (int i = 1; i < remainingPlayers.size(); i++) {
            opponentIndices.add(i);
        }
        
        // Sortuj kandydatów: Ci bez konfliktów Team/City idą na początek listy
        // ZACHOWUJĄC przy tym oryginalny porządek Score w ramach grup
        if (definition.getAvoidSameTeamPairing() || definition.getAvoidSameCityPairing()) {
            opponentIndices.sort((idx1, idx2) -> {
                User opp1 = remainingPlayers.get(idx1);
                User opp2 = remainingPlayers.get(idx2);
                PlayerMetadata meta1 = metadata.get(opp1.getId());
                PlayerMetadata meta2 = metadata.get(opp2.getId());
                
                boolean conflict1 = hasConflict(currentMeta, meta1, definition);
                boolean conflict2 = hasConflict(currentMeta, meta2, definition);
                
                if (conflict1 && !conflict2) return 1; // 1 ma konflikt, 2 nie -> 2 lepszy (return >0 means 1>2?? No, compare(a,b): -1 if a<b. We want non-conflict first.)
                // Wait. Comparator: negative if first is smaller/better.
                // We want NON-CONFLICT (false) before CONFLICT (true).
                // False < True.
                if (conflict1 != conflict2) return Boolean.compare(conflict1, conflict2);
                
                // Jeśli oba mają konflikt lub oba nie mają, zachowaj oryginalną kolejność (idx1 vs idx2)
                return Integer.compare(idx1, idx2);
            });
        }

        for (int opponentIndex : opponentIndices) {
            User opponent = remainingPlayers.get(opponentIndex);

            if (havePlayed(previousPairings, currentPlayer.getId(), opponent.getId())) {
                continue;
            }

            // Tworzymy nową listę dla rekurencji
            List<User> nextRemaining = new ArrayList<>(remainingPlayers);
            // Usuwamy w kolejności od większego indeksu, żeby nie popsuć przesunięć
            // opponentIndex jest indeksem w remainingPlayers
            nextRemaining.remove(opponentIndex);
            nextRemaining.remove(0); // currentPlayer

            resultPairs.add(new Pairing(currentPlayer, opponent));
            if (buildPairsWithConstraints(nextRemaining, previousPairings, resultPairs, definition, metadata)) {
                return true;
            }
            resultPairs.remove(resultPairs.size() - 1);
        }

        return false;
    }
    
    private boolean hasConflict(PlayerMetadata p1, PlayerMetadata p2, TournamentRoundDefinition def) {
        if (def.getAvoidSameTeamPairing() && p1.teamId() != null && p2.teamId() != null) {
            if (p1.teamId().equals(p2.teamId())) return true;
        }
        if (def.getAvoidSameCityPairing() && p1.city() != null && p2.city() != null && !p1.city().isEmpty() && !p2.city().isEmpty()) {
            if (p1.city().equalsIgnoreCase(p2.city())) return true;
        }
        return false;
    }


    private record Pairing(User player1, User player2) {}

    private record PairingPlan(List<Pairing> pairs, User byePlayer) {}
    
    /**
     * Losuje numery stołów dla wszystkich meczów
     */
    private void assignRandomTableNumbers(List<Match> matches) {
        int numberOfTables = matches.size();
        List<Integer> tableNumbers = new ArrayList<>();
        for (int i = 1; i <= numberOfTables; i++) {
            tableNumbers.add(i);
        }
        Collections.shuffle(tableNumbers);
        
        for (int i = 0; i < matches.size(); i++) {
            matches.get(i).setTableNumber(tableNumbers.get(i));
        }
    }

    private Optional<Match> tryCreateMatch(
            List<PlayerStats> rankedPlayers,
            int currentPlayerIndex,
            Set<Long> pairedPlayers,
            Set<String> previousPairings,
            TournamentRound currentRound,
            int tableNumber
    ) {
        PlayerStats currentPlayer = rankedPlayers.get(currentPlayerIndex);
        if (pairedPlayers.contains(currentPlayer.getUser().getId())) {
            return Optional.empty();
        }

        Optional<User> opponent = findSuitableOpponent(
                rankedPlayers,
                currentPlayerIndex,
                pairedPlayers,
                previousPairings
        );

        if (opponent.isPresent()) {
            Match match = createMatch(currentPlayer.getUser(), opponent.get(), currentRound, tableNumber);
            pairedPlayers.add(currentPlayer.getUser().getId());
            pairedPlayers.add(opponent.get().getId());
            return Optional.of(match);
        }

        return Optional.empty();
    }

    private Optional<User> findSuitableOpponent(
            List<PlayerStats> rankedPlayers,
            int currentPlayerIndex,
            Set<Long> pairedPlayers,
            Set<String> previousPairings
    ) {
        User currentPlayer = rankedPlayers.get(currentPlayerIndex).getUser();

        // Najpierw próbujemy znaleźć przeciwnika, z którym jeszcze nie grano
        Optional<User> preferredOpponent = findUnplayedOpponent(rankedPlayers, currentPlayerIndex, pairedPlayers, previousPairings);
        if (preferredOpponent.isPresent()) {
            return preferredOpponent;
        }

        // Jeśli nie znaleziono, bierzemy pierwszego dostępnego
        return findAnyAvailableOpponent(rankedPlayers, currentPlayer, pairedPlayers);
    }

    private Optional<User> findUnplayedOpponent(
            List<PlayerStats> rankedPlayers,
            int currentPlayerIndex,
            Set<Long> pairedPlayers,
            Set<String> previousPairings
    ) {
        User currentPlayer = rankedPlayers.get(currentPlayerIndex).getUser();
        return rankedPlayers.stream()
                .skip(currentPlayerIndex + 1)
                .map(PlayerStats::getUser)
                .filter(user -> !pairedPlayers.contains(user.getId()))
                .filter(user -> !havePlayed(previousPairings, currentPlayer.getId(), user.getId()))
                .findFirst();
    }

    private Optional<User> findAnyAvailableOpponent(
            List<PlayerStats> rankedPlayers,
            User currentPlayer,
            Set<Long> pairedPlayers
    ) {
        return rankedPlayers.stream()
                .map(PlayerStats::getUser)
                .filter(user -> !pairedPlayers.contains(user.getId()))
                .filter(user -> !user.equals(currentPlayer))
                .findFirst();
    }

    private Match createMatch(User player1, User player2, TournamentRound round, int tableNumber) {
        TournamentMatch match = new TournamentMatch();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setTournamentRound(round);
        match.setTableNumber(tableNumber);
        // startTime będzie ustawiony dopiero przy rozpoczęciu rundy
        match.setGameDurationMinutes(round.getTournament().getRoundDurationMinutes());
        
        // Tworzenie rund meczu na podstawie defaultRoundNumber z systemu gry
        int numberOfRounds = round.getTournament().getGameSystem().getDefaultRoundNumber();
        for (int i = 1; i <= numberOfRounds; i++) {
            MatchRound matchRound = new MatchRound();
            matchRound.setRoundNumber(i);
            matchRound.setMatch(match);
            match.getRounds().add(matchRound);
        }
        
        return match;
    }

    private void handleOddNumberOfPlayers(
            List<PlayerStats> rankedPlayers,
            Set<Long> pairedPlayers,
            List<Match> matches,
            TournamentRound currentRound,
            int tableNumber
    ) {
        if (pairedPlayers.size() < rankedPlayers.size()) {
            User lastPlayer = rankedPlayers.stream()
                    .filter(stats -> !pairedPlayers.contains(stats.getUser().getId()))
                    .findFirst()
                    .map(PlayerStats::getUser)
                    .orElseThrow();

            Match byeMatch = createByeMatch(lastPlayer, currentRound, tableNumber);
            matches.add(byeMatch);
        }
    }

    private Match createByeMatch(User player, TournamentRound round, int tableNumber) {
        com.tourney.domain.games.TournamentMatch byeMatch = new com.tourney.domain.games.TournamentMatch();
        byeMatch.setPlayer1(player);
        byeMatch.setTournamentRound(round);
        byeMatch.setTableNumber(tableNumber);
        // startTime będzie ustawiony dopiero przy rozpoczęciu rundy
        byeMatch.setGameDurationMinutes(round.getTournament().getRoundDurationMinutes());
        
        // Tworzenie rund meczu na podstawie defaultRoundNumber z systemu gry
        int numberOfRounds = round.getTournament().getGameSystem().getDefaultRoundNumber();
        for (int i = 1; i <= numberOfRounds; i++) {
            MatchRound matchRound = new MatchRound();
            matchRound.setRoundNumber(i);
            matchRound.setMatch(byeMatch);
            byeMatch.getRounds().add(matchRound);
        }
        
        // Automatyczne przypisanie punktów za BYE na podstawie definicji rundy
        assignByePoints(byeMatch, player, round, numberOfRounds);
        
        return byeMatch;
    }
    
    private void assignByePoints(TournamentMatch byeMatch, User player, TournamentRound round, int numberOfRounds) {
        // Pobierz definicję rundy
        Optional<TournamentRoundDefinition> definitionOpt = roundDefinitionRepository
                .findByTournamentIdAndRoundNumber(round.getTournament().getId(), round.getRoundNumber());
        
        if (definitionOpt.isEmpty()) {
            return; // Brak definicji - nie przypisujemy punktów
        }
        
        TournamentRoundDefinition definition = definitionOpt.get();
        Integer byeSmallPoints = definition.getByeSmallPoints();
        Integer byeLargePoints = definition.getByeLargePoints();
        
        if ((byeSmallPoints == null || byeSmallPoints == 0) && (byeLargePoints == null || byeLargePoints == 0)) {
            return; // Brak zdefiniowanych punktów BYE
        }
        
        // Utwórz MatchResult
        MatchResult matchResult = new MatchResult();
        matchResult.setSubmittedById(player.getId());
        matchResult.setSubmissionTime(LocalDateTime.now());
        matchResult.setWinnerId(player.getId()); // Gracz z BYE automatycznie wygrywa
        
        // Utwórz PlayerScore dla gracza
        PlayerScore playerScore = new PlayerScore();
        
        // Dodaj punkty BYE TYLKO DO PIERWSZEJ RUNDY (nie mnożyć przez liczbę rund)
        RoundScore roundScore = new RoundScore();
        
        // TYLKO Małe punkty (Match Points / SP) -> MAIN_SCORE
        // Tournament Points (TP) będą przypisane bezpośrednio w ParticipantStatsUpdateService
        if (byeSmallPoints != null && byeSmallPoints > 0) {
            roundScore.getScores().put(ScoreType.MAIN_SCORE, byeSmallPoints.doubleValue());
        }
        
        playerScore.getRoundScores().add(roundScore);
        
        // Dla pozostałych rund dodaj puste RoundScore (aby zachować spójność struktury)
        for (int i = 1; i < numberOfRounds; i++) {
            playerScore.getRoundScores().add(new RoundScore());
        }
        
        // Dodaj PlayerScore do MatchResult
        matchResult.addPlayerResult(player.getId(), playerScore);
        
        // Przypisz MatchResult do meczu i oznacz jako zakończony
        byeMatch.setMatchResult(matchResult);
        byeMatch.setCompleted(true);
        byeMatch.setResultsConfirmed(true);
        byeMatch.setPlayer1Confirmed(true);
        byeMatch.setStatus(MatchStatus.COMPLETED);
    }
}