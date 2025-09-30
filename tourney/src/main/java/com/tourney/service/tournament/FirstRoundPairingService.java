package com.tourney.service.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.domain.user.User;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.tournament.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FirstRoundPairingService {
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;

    public List<Match> createFirstRoundPairings(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));

        validatePlayerCount(tournament);
        
        List<User> players = new ArrayList<>(tournament.getParticipants());
        Collections.shuffle(players);
        
        TournamentRound firstRound = findFirstRound(tournament);
        List<Match> matches = createPairings(players, firstRound);
        
        return matchRepository.saveAll(matches);
    }

    private void validatePlayerCount(Tournament tournament) {
        if (tournament.getParticipants().size() < 2) {
            throw new RuntimeException("Za mało graczy do utworzenia par (minimum 2 graczy)");
        }
    }

    private TournamentRound findFirstRound(Tournament tournament) {
        return tournament.getRounds().stream()
                .filter(round -> round.getRoundNumber() == 1)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nie znaleziono pierwszej rundy turnieju"));
    }

    private List<Match> createPairings(List<User> players, TournamentRound round) {
        List<Match> matches = new ArrayList<>();
        
        // Parowanie graczy
        for (int i = 0; i < players.size() - 1; i += 2) {
            matches.add(createMatch(
                players.get(i),
                players.get(i + 1),
                round,
                (i / 2) + 1
            ));
        }

        // Obsługa nieparzystej liczby graczy
        if (players.size() % 2 != 0) {
            matches.add(createByeMatch(
                players.get(players.size() - 1),
                round,
                matches.size() + 1
            ));
        }

        return matches;
    }

    private Match createMatch(User player1, User player2, TournamentRound round, int tableNumber) {
        Match match = new Match();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setTournamentRound(round);
        match.setStartTime(LocalDateTime.now());
        match.setTableNumber(tableNumber);
        match.setGameDurationMinutes(round.getTournament().getRoundDurationMinutes());
        return match;
    }

    private Match createByeMatch(User player, TournamentRound round, int tableNumber) {
        Match match = new Match();
        match.setPlayer1(player);
        match.setTournamentRound(round);
        match.setStartTime(LocalDateTime.now());
        match.setTableNumber(tableNumber);
        match.setGameDurationMinutes(round.getTournament().getRoundDurationMinutes());
        return match;
    }
}