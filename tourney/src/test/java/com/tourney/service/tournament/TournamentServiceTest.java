package com.tourney.service.tournament;

import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.tournament.ActiveTournamentDTO;
import com.tourney.mapper.tournament.TournamentMapper;
import com.tourney.repository.tournament.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TournamentMapper tournamentMapper;

    @InjectMocks
    private TournamentService tournamentService;

    private Long testPlayerId;
    private Tournament tournament1;
    private Tournament tournament2;
    private ActiveTournamentDTO activeDTO1;
    private ActiveTournamentDTO activeDTO2;

    @BeforeEach
    void setUp() {
        testPlayerId = 1L;
        
        tournament1 = new Tournament();
        tournament2 = new Tournament();
        
        activeDTO1 = ActiveTournamentDTO.builder()
                .tournamentId(1L)
                .tournamentName("Tournament 1")
                .currentRound(1)
                .build();
        activeDTO2 = ActiveTournamentDTO.builder()
                .tournamentId(2L)
                .tournamentName("Tournament 2")
                .currentRound(2)
                .build();
    }

    @Test
    void testGetActiveTournaments_withMultipleTournaments() {
        // Given
        List<Tournament> tournaments = Arrays.asList(tournament1, tournament2);
        
        when(tournamentRepository.findActiveForPlayer(testPlayerId)).thenReturn((List)tournaments);
        when(tournamentMapper.toActiveDTO(tournament1, testPlayerId)).thenReturn(activeDTO1);
        when(tournamentMapper.toActiveDTO(tournament2, testPlayerId)).thenReturn(activeDTO2);

        // When
        List<ActiveTournamentDTO> result = tournamentService.getActiveTournaments(testPlayerId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(activeDTO1));
        assertTrue(result.contains(activeDTO2));
        
        verify(tournamentRepository, times(1)).findActiveForPlayer(testPlayerId);
        verify(tournamentMapper, times(1)).toActiveDTO(tournament1, testPlayerId);
        verify(tournamentMapper, times(1)).toActiveDTO(tournament2, testPlayerId);
    }

    @Test
    void testGetActiveTournaments_withNoTournaments() {
        // Given
        when(tournamentRepository.findActiveForPlayer(testPlayerId)).thenReturn((List)Collections.emptyList());

        // When
        List<ActiveTournamentDTO> result = tournamentService.getActiveTournaments(testPlayerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(tournamentRepository, times(1)).findActiveForPlayer(testPlayerId);
        verify(tournamentMapper, never()).toActiveDTO(any(), any());
    }

    @Test
    void testGetActiveTournaments_withSingleTournament() {
        // Given
        List<Tournament> tournaments = Collections.singletonList(tournament1);
        
        when(tournamentRepository.findActiveForPlayer(testPlayerId)).thenReturn((List)tournaments);
        when(tournamentMapper.toActiveDTO(tournament1, testPlayerId)).thenReturn(activeDTO1);

        // When
        List<ActiveTournamentDTO> result = tournamentService.getActiveTournaments(testPlayerId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(activeDTO1, result.get(0));
        
        verify(tournamentRepository, times(1)).findActiveForPlayer(testPlayerId);
        verify(tournamentMapper, times(1)).toActiveDTO(tournament1, testPlayerId);
    }

    @Test
    void testGetActiveTournaments_withDifferentPlayerIds() {
        // Given
        Long playerId1 = 1L;
        Long playerId2 = 2L;
        List<Tournament> tournaments1 = Collections.singletonList(tournament1);
        List<Tournament> tournaments2 = Collections.singletonList(tournament2);
        
        when(tournamentRepository.findActiveForPlayer(playerId1)).thenReturn((List)tournaments1);
        when(tournamentRepository.findActiveForPlayer(playerId2)).thenReturn((List)tournaments2);
        when(tournamentMapper.toActiveDTO(tournament1, playerId1)).thenReturn(activeDTO1);
        when(tournamentMapper.toActiveDTO(tournament2, playerId2)).thenReturn(activeDTO2);

        // When
        List<ActiveTournamentDTO> result1 = tournamentService.getActiveTournaments(playerId1);
        List<ActiveTournamentDTO> result2 = tournamentService.getActiveTournaments(playerId2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        
        verify(tournamentRepository, times(1)).findActiveForPlayer(playerId1);
        verify(tournamentRepository, times(1)).findActiveForPlayer(playerId2);
    }

    @Test
    void testGetActiveTournaments_mapperCalledWithCorrectParameters() {
        // Given
        List<Tournament> tournaments = Collections.singletonList(tournament1);
        when(tournamentRepository.findActiveForPlayer(testPlayerId)).thenReturn((List)tournaments);
        when(tournamentMapper.toActiveDTO(tournament1, testPlayerId)).thenReturn(activeDTO1);

        // When
        tournamentService.getActiveTournaments(testPlayerId);

        // Then
        verify(tournamentMapper).toActiveDTO(eq(tournament1), eq(testPlayerId));
    }
}
