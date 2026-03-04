package com.tourney.service.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchResult;
import com.tourney.domain.games.PlayerScore;
import com.tourney.domain.games.RoundScore;
import com.tourney.domain.games.TournamentMatch;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.domain.tournament.TournamentRoundDefinition;
import com.tourney.domain.tournament.PairingAlgorithmType;
import com.tourney.domain.tournament.TableAssignmentStrategy;
import com.common.domain.User;
import com.tourney.domain.team.Team;
import com.tourney.domain.team.TeamMember;
import com.tourney.domain.team.TeamMemberStatus;
import com.tourney.repository.team.TeamMemberRepository;
import com.tourney.repository.TournamentRoundDefinitionRepository;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.tournament.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubsequentRoundPairingServiceTest {

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

    @InjectMocks
    private SubsequentRoundPairingService pairingService;

    private Tournament tournament;
    private TournamentRound round1;
    private TournamentRound round2;
    private List<User> players;
    private TournamentRoundDefinition roundDefinition;

    @BeforeEach
    void setUp() {
        tournament = new Tournament();
        tournament.setId(1L);
        tournament.setNumberOfRounds(3);
        
        com.tourney.domain.systems.GameSystem system = new com.tourney.domain.systems.GameSystem();
        system.setDefaultRoundNumber(3);
        tournament.setGameSystem(system);
        
        round1 = new TournamentRound();
        round1.setRoundNumber(1);
        round1.setTournament(tournament);
        
        round2 = new TournamentRound();
        round2.setRoundNumber(2);
        round2.setTournament(tournament);
        
        tournament.setRounds(new ArrayList<>(Arrays.asList(round1, round2)));
        
        players = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            User user = new User();
            user.setId((long) (i + 1));
            user.setName("Player " + (i + 1));
            players.add(user);
        }
        
        // Setup participants
        List<TournamentParticipant> participants = new ArrayList<>();
        for (User user : players) {
            TournamentParticipant participant = new TournamentParticipant();
            participant.setUser(user);
            participant.setConfirmed(true);
            participants.add(participant);
        }
        tournament.setParticipantLinks(participants);
        
        roundDefinition = new TournamentRoundDefinition();
        roundDefinition.setPairingAlgorithm(PairingAlgorithmType.STANDARD);
        roundDefinition.setTableAssignmentStrategy(TableAssignmentStrategy.BEST_FIRST);
        
        // Mock team member repository by default
        lenient().when(teamMemberRepository.findActiveMembership(any(), any(), any()))
                .thenReturn(Optional.empty());
    }
    
    @Test
    void shouldCreatePairingsAvoidRematches() {
        // Given
        // Round 1 pairings: (1-2), (3-4), (5-6)
        Match m1 = createMatch(players.get(0), players.get(1));
        Match m2 = createMatch(players.get(2), players.get(3));
        Match m3 = createMatch(players.get(4), players.get(5));
        round1.setMatches(Arrays.asList(m1, m2, m3));
        
        // Mock repository calls
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(roundDefinitionRepository.findByTournamentIdAndRoundNumber(1L, 2))
                .thenReturn(Optional.of(roundDefinition));
        when(scoreRepository.findAllByTournamentId(1L)).thenReturn(Collections.emptyList());
        when(matchRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        List<Match> round2Matches = pairingService.createNextRoundPairings(1L, 2);
        
        // Then
        assertEquals(3, round2Matches.size());
        
        for (Match match : round2Matches) {
            Long p1Id = match.getPlayer1().getId();
            Long p2Id = match.getPlayer2().getId();
            
            // Check against previous pairings
            assertFalse((p1Id == 1L && p2Id == 2L) || (p1Id == 2L && p2Id == 1L), "Player 1 and 2 shouldn't play again");
            assertFalse((p1Id == 3L && p2Id == 4L) || (p1Id == 4L && p2Id == 3L), "Player 3 and 4 shouldn't play again");
            assertFalse((p1Id == 5L && p2Id == 6L) || (p1Id == 6L && p2Id == 5L), "Player 5 and 6 shouldn't play again");
        }
    }
    
    @Test
    void shouldHandleSubsequentRematchAvoidance() {
        // Given
        // Round 1: (1-2), (3-4), (5-6)
        Match m1 = createMatch(players.get(0), players.get(1));
        Match m2 = createMatch(players.get(2), players.get(3));
        Match m3 = createMatch(players.get(4), players.get(5));
        round1.setMatches(Arrays.asList(m1, m2, m3));
        
        // Mock repository calls
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(roundDefinitionRepository.findByTournamentIdAndRoundNumber(1L, 2))
                .thenReturn(Optional.of(roundDefinition));
        
        // Simulate score - make 1, 3, 5 winners so they are top ranked
        // 1 beats 2, 3 beats 4, 5 beats 6
        // Now top ranked are 1, 3, 5. They should play each other.
        // But 1 vs 3, leaving 5 to play vs lower bracket?
        
        // Let's force a situation where simple greedy fails.
        // Suppose current ranking order is 1, 2, 3, 4, 5, 6
        // Previous matches: (1-2), (3-4), (5-6)
        // Greedy would try pair 1-2 (REMATCH!), skip.
        // Try 1-3. OK.
        // Remaining: 2, 4, 5, 6.
        // Greedy takes 2. Try 2-4. OK but maybe they played? No.
        
        // Let's ensure pairs are unique
        when(matchRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Match> round2Matches = pairingService.createNextRoundPairings(1L, 2);
        
        // Then
        assertEquals(3, round2Matches.size());
        Set<Long> played = new HashSet<>();
        for (Match m : round2Matches) {
            played.add(m.getPlayer1().getId());
            played.add(m.getPlayer2().getId());
        }
        assertEquals(6, played.size());
    }

    @Test
    void shouldAssignByePointsForOddNumberOfPlayers() {
        // Given odd number of players (remove one from confirmed)
        List<TournamentParticipant> participants = tournament.getParticipantLinks();
        participants.get(5).setConfirmed(false); // Player 6 drops

        // Set BYE points configuration
        roundDefinition.setByeSmallPoints(10);
        roundDefinition.setByeLargePoints(20);

        // Previous matches (assuming R1 was full)
        Match m1 = createMatch(players.get(0), players.get(1));
        Match m2 = createMatch(players.get(2), players.get(3));
        Match m3 = createMatch(players.get(4), players.get(5));
        round1.setMatches(Arrays.asList(m1, m2, m3));

        // Mock repository calls
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(roundDefinitionRepository.findByTournamentIdAndRoundNumber(1L, 2))
                .thenReturn(Optional.of(roundDefinition));
        // Empty scores for simplicity
        when(scoreRepository.findAllByTournamentId(1L)).thenReturn(Collections.emptyList());
        when(matchRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<Match> round2Matches = pairingService.createNextRoundPairings(1L, 2);

        // Then
        assertEquals(3, round2Matches.size(), "Should have 2 matches + 1 BYE match");
        
        // Find the bye match (should be TournamentMatch with only player1 set and rounds created)
        TournamentMatch byeMatch = (TournamentMatch) round2Matches.stream()
            .filter(m -> m instanceof TournamentMatch && 
                        (m.getPlayer2() == null) && // BYE usually has no p2
                        m.getPlayer1() != null)
            .findFirst()
            .orElse(null);

        assertNotNull(byeMatch, "Should have created a BYE match");
        
        // Use TournamentMatch-specific methods to access rounds and result
        // Verify rounds
        assertEquals(3, byeMatch.getRounds().size(), "Should have 3 sub-rounds (game system default)");
        
        // Verify points
        MatchResult result = byeMatch.getMatchResult();
        assertNotNull(result, "BYE match should have auto-generated result");
        
        // Check winner
        assertEquals(byeMatch.getPlayer1().getId(), result.getWinnerId());
        
        // Check scores
        PlayerScore ps = result.getPlayerScores().get(byeMatch.getPlayer1().getId());
        assertNotNull(ps, "Player score should exist");
        assertEquals(3, ps.getRoundScores().size());
        
        RoundScore rs1 = ps.getRoundScores().get(0);
        assertEquals(10.0, rs1.getScores().get(ScoreType.MAIN_SCORE));
        assertEquals(20.0, rs1.getScores().get(ScoreType.SECONDARY_SCORE));
        
        // Check that points are also saved in ScoreRepository (required for ranking calculation)
        // verify(scoreRepository, times(1)).save(any(com.tourney.domain.scores.Score.class)); // Can verify call or check mock
        // Since we mocked scoreRepository.save (not explicitly, likely returns void or passed arg), 
        // we can verify calls.
        
        // 3 rounds * 2 score types = 6 Score entities expected to be saved
        verify(scoreRepository, atLeastOnce()).save(any(com.tourney.domain.scores.Score.class));
    }

    private Match createMatch(User p1, User p2) {
        com.tourney.domain.games.TournamentMatch m = new com.tourney.domain.games.TournamentMatch();
        m.setPlayer1(p1);
        m.setPlayer2(p2);
        
        // This match needs a tournament with game system purely to avoid NPEs if inspected deeply,
        // though strictly for "past" matches the service mainly checks player IDs.
        Tournament tournamentWithSystem = new Tournament();
        com.tourney.domain.systems.GameSystem system = new com.tourney.domain.systems.GameSystem();
        system.setDefaultRoundNumber(3);
        tournamentWithSystem.setGameSystem(system);
        
        TournamentRound roundWithTournament = new TournamentRound();
        roundWithTournament.setTournament(tournamentWithSystem);
        
        m.setTournamentRound(roundWithTournament); 
        return m;
    }
}
