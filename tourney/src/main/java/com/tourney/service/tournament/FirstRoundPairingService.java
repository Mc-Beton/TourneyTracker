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
import com.tourney.domain.tournament.TournamentChallenge;
import com.tourney.domain.tournament.ChallengeStatus;
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
        
        matches.addAll(createPairings(remainingPlayers, firstRound, tableNumber));
        
        // Zmiana fazy turnieju - pary dobrane, czeka na start
        tournament.setPhase(TournamentPhase.PAIRINGS_READY);
        tournamentRepository.save(tournament);
        
        return matchRepository.saveAll(matches);
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