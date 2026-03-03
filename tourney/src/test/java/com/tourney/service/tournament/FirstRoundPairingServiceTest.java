package com.tourney.service.tournament;

import com.common.domain.User;
import com.tourney.domain.games.Match;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.tournament.*;
import com.tourney.repository.TournamentRoundDefinitionRepository;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.tournament.TournamentChallengeRepository;
import com.tourney.repository.tournament.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirstRoundPairingServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TournamentRoundDefinitionRepository roundDefinitionRepository;

    @Mock
    private TournamentChallengeRepository challengeRepository;

    @InjectMocks
    private FirstRoundPairingService firstRoundPairingService;

    private Tournament tournament;
    private List<User> players;
    private List<TournamentParticipant> participants;
    private TournamentRoundDefinition roundDefinition;

    @BeforeEach
    void setUp() {
        tournament = new Tournament();
        tournament.setId(1L);
        tournament.setNumberOfRounds(3);
        tournament.setRoundDurationMinutes(60);

        GameSystem gameSystem = new GameSystem();
        gameSystem.setDefaultRoundNumber(3);
        tournament.setGameSystem(gameSystem);

        TournamentRound round1 = new TournamentRound();
        round1.setRoundNumber(1);
        round1.setTournament(tournament);
        tournament.setRounds(Collections.singletonList(round1));

        players = new ArrayList<>();
        participants = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            User user = new User();
            user.setId((long) (i + 1));
            user.setName("Player " + (i + 1));
            players.add(user);

            TournamentParticipant participant = new TournamentParticipant();
            participant.setUser(user);
            participant.setConfirmed(true);
            participants.add(participant);
        }
        tournament.setParticipantLinks(participants);

        roundDefinition = new TournamentRoundDefinition();
        roundDefinition.setRoundNumber(1);
        roundDefinition.setPairingAlgorithm(PairingAlgorithmType.STANDARD);
        roundDefinition.setPlayerLevelPairingStrategy(PlayerLevelPairingStrategy.NONE);

        // Lenient stubs for common calls
        lenient().when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        lenient().when(roundDefinitionRepository.findByTournamentIdOrderByRoundNumberAsc(1L))
                .thenReturn(Collections.singletonList(roundDefinition));
        lenient().when(matchRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldPrioritizeAcceptedChallenges() {
        // Arrange
        User p1 = players.get(0);
        User p2 = players.get(1);

        TournamentChallenge challenge = new TournamentChallenge();
        challenge.setChallenger(p1);
        challenge.setOpponent(p2);
        challenge.setStatus(ChallengeStatus.ACCEPTED);
        challenge.setTournament(tournament);
        
        when(challengeRepository.findAllByTournamentIdAndStatus(1L, ChallengeStatus.ACCEPTED))
                .thenReturn(Collections.singletonList(challenge));

        // Act
        List<Match> result = firstRoundPairingService.createFirstRoundPairings(1L);

        // Assert
        assertEquals(3, result.size(), "Should verify 3 matches generated for 6 players");

        // Verify P1 vs P2 exists
        boolean challengePairFound = result.stream().anyMatch(match -> 
            (match.getPlayer1().equals(p1) && match.getPlayer2().equals(p2)) ||
            (match.getPlayer1().equals(p2) && match.getPlayer2().equals(p1))
        );
        assertTrue(challengePairFound, "Player 1 and Player 2 should be paired due to ACCEPTED challenge");
    }

    @Test
    void shouldIgnorePendingChallenges() {
        // Arrange
        // No ACCEPTED challenges returned from repo
        when(challengeRepository.findAllByTournamentIdAndStatus(1L, ChallengeStatus.ACCEPTED))
                .thenReturn(Collections.emptyList());

        // Act
        List<Match> result = firstRoundPairingService.createFirstRoundPairings(1L);

        // Assert
        assertEquals(3, result.size());
        // Since it's shuffled, we can't assert specific pairings easily, 
        // but logic verifies it falls through to standard pairing.
    }

    @Test
    void shouldHandleChallengeWithOneUnconfirmedPlayer() {
        // Arrange
        User p1 = players.get(0);
        User p2 = players.get(1);
        
        // Mark P2 as unconfirmed
        participants.get(1).setConfirmed(false);

        TournamentChallenge challenge = new TournamentChallenge();
        challenge.setChallenger(p1);
        challenge.setOpponent(p2);
        challenge.setStatus(ChallengeStatus.ACCEPTED);
        
        when(challengeRepository.findAllByTournamentIdAndStatus(1L, ChallengeStatus.ACCEPTED))
                .thenReturn(Collections.singletonList(challenge));

        // Act
        List<Match> result = firstRoundPairingService.createFirstRoundPairings(1L);
        
        // Assert
        // Logic: if unconfirmed, challenge is ignored. 
        // Total confirmed players = 5. So 2 matches + 1 bye?
        // Service should handle 5 players.
        
        // Logic for odd players: createPairings handles (size-1)/2 matches + 1 bye match.
        // Size = 5. (5-1)/2 = 2 standard matches. 1 bye match. Total 3 matches.
        assertEquals(3, result.size());
        
        // Check pairing logic didn't force P1 vs P2 (since P2 is not in pool)
        boolean p2InMatches = result.stream().anyMatch(m -> 
             (m.getPlayer1() != null && m.getPlayer1().equals(p2)) || 
             (m.getPlayer2() != null && m.getPlayer2().equals(p2))
        );
        assertTrue(!p2InMatches, "Unconfirmed player P2 should not be in matches");
    }
}
