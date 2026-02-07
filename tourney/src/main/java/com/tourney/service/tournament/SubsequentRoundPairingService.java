package com.tourney.service.tournament;

import com.tourney.domain.games.*;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.player.PlayerStats;
import com.tourney.domain.scores.Score;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.domain.tournament.TournamentRoundDefinition;
import com.common.domain.User;
import com.tourney.repository.TournamentRoundDefinitionRepository;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.tournament.TournamentRepository;
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

    public List<Match> createNextRoundPairings(Long tournamentId, int roundNumber) {
        Tournament tournament = getTournament(tournamentId);
        TournamentRound currentRound = findTournamentRound(tournament, roundNumber);
        
        List<PlayerStats> rankedPlayers = getRankedPlayers(tournament);
        Set<String> previousPairings = getPreviousPairings(tournament);
        
        List<Match> newMatches = createMatches(rankedPlayers, previousPairings, currentRound);
        
        return matchRepository.saveAll(newMatches);
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

    private List<Match> createMatches(List<PlayerStats> rankedPlayers, Set<String> previousPairings, TournamentRound currentRound) {
        List<Match> matches = new ArrayList<>();
        Set<Long> pairedPlayers = new HashSet<>();

        // Parowanie główne
        for (int i = 0; i < rankedPlayers.size(); i++) {
            if (pairedPlayers.contains(rankedPlayers.get(i).getUser().getId())) {
                continue;
            }

            Optional<Match> match = tryCreateMatch(rankedPlayers, i, pairedPlayers, previousPairings, currentRound);
            match.ifPresent(matches::add);
        }

        // Obsługa nieparzystej liczby graczy
        handleOddNumberOfPlayers(rankedPlayers, pairedPlayers, matches, currentRound);

        return matches;
    }

    private Optional<Match> tryCreateMatch(
            List<PlayerStats> rankedPlayers,
            int currentPlayerIndex,
            Set<Long> pairedPlayers,
            Set<String> previousPairings,
            TournamentRound currentRound
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
            Match match = createMatch(currentPlayer.getUser(), opponent.get(), currentRound);
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

    private Match createMatch(User player1, User player2, TournamentRound round) {
        TournamentMatch match = new TournamentMatch();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setTournamentRound(round);
        match.setStartTime(LocalDateTime.now());
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
            TournamentRound currentRound
    ) {
        if (pairedPlayers.size() < rankedPlayers.size()) {
            User lastPlayer = rankedPlayers.stream()
                    .filter(stats -> !pairedPlayers.contains(stats.getUser().getId()))
                    .findFirst()
                    .map(PlayerStats::getUser)
                    .orElseThrow();

            Match byeMatch = createByeMatch(lastPlayer, currentRound);
            matches.add(byeMatch);
        }
    }

    private Match createByeMatch(User player, TournamentRound round) {
        com.tourney.domain.games.TournamentMatch byeMatch = new com.tourney.domain.games.TournamentMatch();
        byeMatch.setPlayer1(player);
        byeMatch.setTournamentRound(round);
        byeMatch.setStartTime(LocalDateTime.now());
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
        
        if (byeSmallPoints == null && byeLargePoints == null) {
            return; // Brak zdefiniowanych punktów BYE
        }
        
        // Utwórz MatchResult
        MatchResult matchResult = new MatchResult();
        matchResult.setSubmittedById(player.getId());
        matchResult.setSubmissionTime(LocalDateTime.now());
        matchResult.setWinnerId(player.getId()); // Gracz z BYE automatycznie wygrywa
        
        // Utwórz PlayerScore dla gracza
        PlayerScore playerScore = new PlayerScore();
        
        // Dla każdej rundy meczu dodaj punkty BYE
        for (int i = 0; i < numberOfRounds; i++) {
            RoundScore roundScore = new RoundScore();
            
            // Małe punkty (Primary points) -> MAIN_SCORE
            if (byeSmallPoints != null && byeSmallPoints > 0) {
                roundScore.getScores().put(ScoreType.MAIN_SCORE, byeSmallPoints.doubleValue());
            }
            
            // Duże punkty (Game points) -> SECONDARY_SCORE
            if (byeLargePoints != null && byeLargePoints > 0) {
                roundScore.getScores().put(ScoreType.SECONDARY_SCORE, byeLargePoints.doubleValue());
            }
            
            playerScore.getRoundScores().add(roundScore);
        }
        
        // Dodaj PlayerScore do MatchResult
        matchResult.addPlayerResult(player.getId(), playerScore);
        
        // Przypisz MatchResult do meczu
        byeMatch.setMatchResult(matchResult);
        byeMatch.setCompleted(true);
        byeMatch.setResultsConfirmed(true);
        byeMatch.setPlayer1Confirmed(true);
    }
}