package com.tourney.service.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchRound;
import com.tourney.domain.games.TournamentMatch;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.tournament.PairingAlgorithmType;
import com.tourney.domain.tournament.PlayerLevelPairingStrategy;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentPhase;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.domain.tournament.TournamentRoundDefinition;
import com.common.domain.User;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.repository.TournamentRoundDefinitionRepository;
import com.tourney.repository.tournament.TournamentChallengeRepository;
import com.tourney.domain.team.TeamMember;
import com.tourney.domain.team.TeamMemberStatus;
import com.tourney.repository.team.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FirstRoundPairingService {
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final TournamentRoundDefinitionRepository roundDefinitionRepository;
    private final TournamentChallengeRepository challengeRepository;
    private final TeamMemberRepository teamMemberRepository;

    public List<Match> createFirstRoundPairings(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));
        
        TournamentRound firstRound = findFirstRound(tournament);
        
        validatePlayerCount(tournament);

        List<User> confirmedPlayers = new ArrayList<>(
                tournament.getParticipantLinks().stream()
                        .filter(TournamentParticipant::isConfirmed)
                        .map(TournamentParticipant::getUser)
                        .toList()
        );

        List<Match> matches = new ArrayList<>();
        int tableNumber = 1;

        // 1. Handle Accepted Challenges
        Set<Long> pairedPlayerIds = new HashSet<>();
        List<TournamentChallenge> acceptedChallenges = challengeRepository.findAllByTournamentIdAndStatus(tournamentId, ChallengeStatus.ACCEPTED);
        
        for (TournamentChallenge challenge : acceptedChallenges) {
            User p1 = challenge.getChallenger();
            User p2 = challenge.getOpponent();
            
            // Check if both are still confirmed participants
            if (confirmedPlayers.contains(p1) && confirmedPlayers.contains(p2)) {
                matches.add(createMatch(p1, p2, firstRound, tableNumber));
                tableNumber++;
                
                pairedPlayerIds.add(p1.getId());
                pairedPlayerIds.add(p2.getId());
            }
        }
        
        // Remove challenged players from pool
        List<User> remainingPlayers = confirmedPlayers.stream()
                .filter(u -> !pairedPlayerIds.contains(u.getId()))
                .collect(Collectors.toList());

        // 2. Standard pairing logic for remaining
        // Pobierz definicję pierwszej rundy
        TournamentRoundDefinition roundDefinition = roundDefinitionRepository
                .findByTournamentIdOrderByRoundNumberAsc(tournamentId)
                .stream()
                .filter(def -> def.getRoundNumber() == 1)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nie znaleziono definicji pierwszej rundy"));

        // Dobór algorytmu parowania na podstawie definicji rundy
        PairingAlgorithmType algorithmType = roundDefinition.getPairingAlgorithm();
        
        if (algorithmType == PairingAlgorithmType.CUSTOM) {
            // Custom algorytm - stosujemy wybrane strategie
            PlayerLevelPairingStrategy strategy = roundDefinition.getPlayerLevelPairingStrategy();
            
            if (strategy == PlayerLevelPairingStrategy.NONE) {
                // Brak preferencji - zwykłe losowanie
                Collections.shuffle(remainingPlayers);
            } else if (strategy == PlayerLevelPairingStrategy.BEGINNERS_WITH_VETERANS) {
                // Parowanie początkujących z weteranami
                applyBeginnersWithVeteransStrategy(remainingPlayers);
            } else if (strategy == PlayerLevelPairingStrategy.BEGINNERS_WITH_BEGINNERS) {
                // Parowanie podobnych poziomów
                applyBeginnersWithBeginnersStrategy(remainingPlayers);
            } else {
                // Fallback - jeśli strategia nie jest rozpoznana
                Collections.shuffle(remainingPlayers);
            }
        } else {
            // STANDARD - losowe przetasowanie
            Collections.shuffle(remainingPlayers);
        }
        
        // Zastosowanie ograniczeń Team/City (jeśli włączone)
        List<Match> generatedMatches;
        if (roundDefinition.getAvoidSameTeamPairing() || roundDefinition.getAvoidSameCityPairing()) {
            Map<Long, PlayerMetadata> metadata = loadPlayerMetadata(remainingPlayers, tournament);
            generatedMatches = createPairingsWithConstraints(remainingPlayers, firstRound, tableNumber, roundDefinition, metadata);
        } else {
            generatedMatches = createPairings(remainingPlayers, firstRound, tableNumber);
        }

        matches.addAll(generatedMatches);
        
        // Zmiana fazy turnieju - pary dobrane, czeka na start
        tournament.setPhase(TournamentPhase.PAIRINGS_READY);
        tournamentRepository.save(tournament);
        
        return matchRepository.saveAll(matches);
    }
    
    private List<Match> createPairingsWithConstraints(List<User> players, TournamentRound round, int startTableNumber, 
                                                     TournamentRoundDefinition definition, Map<Long, PlayerMetadata> metadata) {
        // Używamy backtrackingu, aby znaleźć permutację spełniającą warunki
        List<Pairing> pairs = new ArrayList<>();
        // PreviousPairings puste dla 1. rundy
        Set<String> previousPairings = Collections.emptySet();
        
        boolean success = false;
        User byePlayer = null;

        if (players.size() % 2 == 0) {
            if (buildPairsWithConstraints(players, previousPairings, pairs, definition, metadata)) {
                success = true;
            }
        } else {
            // Spróbuj znaleźć BYE gracza, który pozwoli na poprawne sparowanie reszty
            // Zaczynamy od końca listy (tutaj lista jest już potasowana, więc to losowy gracz)
            for (int byeIndex = players.size() - 1; byeIndex >= 0; byeIndex--) {
                byePlayer = players.get(byeIndex);
                List<User> remaining = new ArrayList<>(players);
                remaining.remove(byeIndex);
                
                pairs.clear();
                if (buildPairsWithConstraints(remaining, previousPairings, pairs, definition, metadata)) {
                    success = true;
                    break;
                }
            }
        }
        
        // Fallback: Jeśli nie udało się spełnić ograniczeń, ignorujemy je (używamy standardowego pairingowania na obecnej liście)
        if (!success) {
            return createPairings(players, round, startTableNumber);
        }

        List<Match> result = new ArrayList<>();
        int tableNumber = startTableNumber;
        for (Pairing p : pairs) {
            result.add(createMatch(p.player1, p.player2, round, tableNumber++));
        }
        if (byePlayer != null) {
            result.add(createByeMatch(byePlayer, round, tableNumber++));
        }
        return result;
    }

    // --- Helper methods duplicated from SubsequentRoundPairingService (due to lack of shared component) ---
    private record PlayerMetadata(Long teamId, String city) {}
    private record Pairing(User player1, User player2) {}

    private Map<Long, PlayerMetadata> loadPlayerMetadata(List<User> players, Tournament tournament) {
        Map<Long, PlayerMetadata> metadata = new HashMap<>();
        
        // Pobierz wszystkich członków drużyn (uproszczone)
        List<TeamMember> teamMembers = teamMemberRepository.findAll(); 
        
        for (User player : players) {
            Long teamId = null;
            String city = player.getCity(); 
            
            Optional<TeamMember> member = teamMemberRepository.findActiveMembership(
                    player, 
                    tournament.getGameSystem(), 
                    TeamMemberStatus.ACTIVE
            );
            
            if (member.isPresent()) {
                teamId = member.get().getTeam().getId();
                if (city == null || city.isEmpty()) {
                     city = member.get().getTeam().getCity();
                }
            }
            metadata.put(player.getId(), new PlayerMetadata(teamId, city));
        }
        return metadata;
    }
    
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

        List<Integer> opponentIndices = new ArrayList<>();
        for (int i = 1; i < remainingPlayers.size(); i++) {
            opponentIndices.add(i);
        }
        
        opponentIndices.sort((idx1, idx2) -> {
            User opp1 = remainingPlayers.get(idx1);
            User opp2 = remainingPlayers.get(idx2);
            PlayerMetadata meta1 = metadata.get(opp1.getId());
            PlayerMetadata meta2 = metadata.get(opp2.getId());
            
            boolean conflict1 = hasConflict(currentMeta, meta1, definition);
            boolean conflict2 = hasConflict(currentMeta, meta2, definition);
            
            if (conflict1 != conflict2) return Boolean.compare(conflict1, conflict2);
            return Integer.compare(idx1, idx2);
        });

        for (int opponentIndex : opponentIndices) {
            User opponent = remainingPlayers.get(opponentIndex);

            // createPairingsWithConstraints sends empty previousPairings, so this is always false for Round 1
            if (previousPairings.contains(currentPlayer.getId() + "-" + opponent.getId())) { 
                continue;
            }

            List<User> nextRemaining = new ArrayList<>(remainingPlayers);
            nextRemaining.remove(opponentIndex);
            nextRemaining.remove(0);

            resultPairs.add(new Pairing(currentPlayer, opponent));
            if (buildPairsWithConstraints(nextRemaining, previousPairings, resultPairs, definition, metadata)) {
                return true;
            }
            resultPairs.remove(resultPairs.size() - 1);
        }

        return false;
    }

    private boolean hasConflict(PlayerMetadata p1, PlayerMetadata p2, TournamentRoundDefinition def) {
        if (Boolean.TRUE.equals(def.getAvoidSameTeamPairing()) && p1.teamId() != null && p2.teamId() != null) {
            if (p1.teamId().equals(p2.teamId())) return true;
        }
        if (Boolean.TRUE.equals(def.getAvoidSameCityPairing()) && p1.city() != null && p2.city() != null && !p1.city().isEmpty() && !p2.city().isEmpty()) {
            if (p1.city().equalsIgnoreCase(p2.city())) return true;
        }
        return false;
    }

    private void validatePlayerCount(Tournament tournament) {
        long confirmedCount = tournament.getParticipantLinks().stream()
                .filter(TournamentParticipant::isConfirmed)
                .count();

        if (confirmedCount < 2) {
            throw new RuntimeException("Za mało potwierdzonych graczy do utworzenia par (minimum 2 graczy)");
        }
    }


    private TournamentRound findFirstRound(Tournament tournament) {
        return tournament.getRounds().stream()
                .filter(round -> round.getRoundNumber() == 1)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nie znaleziono pierwszej rundy turnieju"));
    }


    private List<Match> createPairings(List<User> players, TournamentRound round, int startTableNumber) {
        List<Match> matches = new ArrayList<>();
        int tableNumber = startTableNumber;

        // Create pairs for the remaining players (even count)
        for (int i = 0; i < players.size() - 1; i += 2) {
            matches.add(createMatch(players.get(i), players.get(i + 1), round, tableNumber));
            tableNumber++;
        }

        // Handle odd number of players (BYE)
        if (players.size() % 2 != 0) {
            matches.add(createByeMatch(players.get(players.size() - 1), round, tableNumber));
        }
        
        return matches;
    }

    private Match createMatch(User player1, User player2, TournamentRound round, int tableNumber) {
        TournamentMatch match = new TournamentMatch();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setTournamentRound(round);
        // startTime będzie ustawiony dopiero przy rozpoczęciu rundy
        match.setTableNumber(tableNumber);
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

    private Match createByeMatch(User player, TournamentRound round, int tableNumber) {
        TournamentMatch match = new TournamentMatch();
        match.setPlayer1(player);
        match.setTournamentRound(round);
        // startTime będzie ustawiony dopiero przy rozpoczęciu rundy
        match.setTableNumber(tableNumber);
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

    /**
     * Strategia parowania początkujących z weteranami
     * Rozdziela graczy na dwie grupy, tasuje je i łączy w pary (beginner, veteran)
     */
    private void applyBeginnersWithVeteransStrategy(List<User> players) {
        // Rozdzielenie na beginners i veterans
        List<User> beginners = players.stream()
                .filter(user -> Boolean.TRUE.equals(user.getBeginner()))
                .collect(Collectors.toList());
        
        List<User> veterans = players.stream()
                .filter(user -> !Boolean.TRUE.equals(user.getBeginner()))
                .collect(Collectors.toList());
        
        // Losowe tasowanie obu grup
        Collections.shuffle(beginners);
        Collections.shuffle(veterans);
        
        // Czyścimy oryginalną listę i budujemy ją od nowa
        players.clear();
        
        // Parowanie beginners z veterans
        int minSize = Math.min(beginners.size(), veterans.size());
        for (int i = 0; i < minSize; i++) {
            players.add(beginners.get(i));
            players.add(veterans.get(i));
        }
        
        // Dodanie pozostałych graczy (ci, którzy nie mieli pary z innego poziomu)
        if (beginners.size() > minSize) {
            players.addAll(beginners.subList(minSize, beginners.size()));
        }
        if (veterans.size() > minSize) {
            players.addAll(veterans.subList(minSize, veterans.size()));
        }
    }

    /**
     * Strategia parowania graczy tego samego poziomu
     * Grupuje graczy według poziomu i tasuje w ramach grup
     */
    private void applyBeginnersWithBeginnersStrategy(List<User> players) {
        // Rozdzielenie na beginners i veterans
        List<User> beginners = players.stream()
                .filter(user -> Boolean.TRUE.equals(user.getBeginner()))
                .collect(Collectors.toList());
        
        List<User> veterans = players.stream()
                .filter(user -> !Boolean.TRUE.equals(user.getBeginner()))
                .collect(Collectors.toList());
        
        // Losowe tasowanie w ramach każdej grupy
        Collections.shuffle(beginners);
        Collections.shuffle(veterans);
        
        // Czyścimy oryginalną listę i budujemy ją od nowa
        players.clear();
        
        // Dodaj najpierw beginners, potem veterans
        // Parowanie sekwencyjne w createPairings() spowoduje,
        // że beginners będą parowani z beginners, veterans z veterans
        players.addAll(beginners);
        players.addAll(veterans);
    }
}