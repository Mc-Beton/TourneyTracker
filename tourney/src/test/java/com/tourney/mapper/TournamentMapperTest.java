package com.tourney.mapper;

import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.systems.StandardGameSystem;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentScoring;
import com.tourney.domain.scores.ScoreType;
import com.tourney.domain.scores.ScoringSystem;
import com.tourney.domain.user.User;
import com.tourney.dto.tournament.TournamentResponseDTO;
import com.tourney.mapper.tournament.TournamentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TournamentMapperTest {

    @InjectMocks
    private TournamentMapper mapper;

    @Test
    void shouldMapTournamentToDto() {
        // Given
        Tournament tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Test Tournament");
        tournament.setStartDate(LocalDate.now());
        tournament.setNumberOfRounds(3);
        tournament.setRoundDurationMinutes(60);

        GameSystem gameSystem = new GameSystem();
        gameSystem.setId(1L);
        gameSystem.setName("Standard System");
        tournament.setGameSystem(gameSystem);

        User organizer = new User();
        organizer.setId(1L);
        tournament.setOrganizer(organizer);

        User participant1 = new User();
        participant1.setId(2L);
        User participant2 = new User();
        participant2.setId(3L);
        tournament.setParticipants(Arrays.asList(participant1, participant2));

        TournamentScoring scoring = new TournamentScoring();
        scoring.setScoringSystem(ScoringSystem.ROUND_BY_ROUND);
        scoring.setEnabledScoreTypes(Set.of(ScoreType.MAIN_SCORE));
        scoring.setRequireAllScoreTypes(true);
        scoring.setMinScore(0);
        scoring.setMaxScore(100);
        tournament.setTournamentScoring(scoring);

        // When
        TournamentResponseDTO dto = mapper.toDto(tournament);

        // Then
        assertNotNull(dto);
        assertEquals(tournament.getId(), dto.getId());
        assertEquals(tournament.getName(), dto.getName());
        assertEquals(tournament.getStartDate(), dto.getStartDate());
        assertEquals(tournament.getNumberOfRounds(), dto.getNumberOfRounds());
        assertEquals(tournament.getRoundDurationMinutes(), dto.getRoundDurationMinutes());
        assertEquals(tournament.getGameSystem().getId(), dto.getGameSystemId());
        assertEquals(tournament.getOrganizer().getId(), dto.getOrganizerId());
        assertEquals(2, dto.getParticipantIds().size());
        assertTrue(dto.getParticipantIds().contains(2L));
        assertTrue(dto.getParticipantIds().contains(3L));
        assertEquals(ScoringSystem.ROUND_BY_ROUND, dto.getScoringSystem());
        assertEquals(Set.of(ScoreType.MAIN_SCORE), dto.getEnabledScoreTypes());
        assertTrue(dto.isRequireAllScoreTypes());
        assertEquals(0, dto.getMinScore());
        assertEquals(100, dto.getMaxScore());
    }

    @Test
    void shouldReturnNullForNullInput() {
        assertNull(mapper.toDto(null));
    }
}