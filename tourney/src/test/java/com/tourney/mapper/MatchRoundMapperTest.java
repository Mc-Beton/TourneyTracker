
package com.tourney.mapper;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.MatchRound;
import com.tourney.dto.games.MatchRoundDTO;
import com.tourney.mapper.games.MatchRoundMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MatchRoundMapperTest {

    private final MatchRoundMapper mapper = new MatchRoundMapper();

    @Test
    void shouldMapToDto() {
        // Given
        Match match = new Match();
        match.setId(1L);

        MatchRound matchRound = new MatchRound();
        matchRound.setId(1L);
        matchRound.setMatch(match);
        matchRound.setStartTime(LocalDateTime.now());
        matchRound.setEndTime(LocalDateTime.now().plusHours(1));

        // When
        MatchRoundDTO dto = mapper.toDto(matchRound);

        // Then
        assertNotNull(dto);
        assertEquals(matchRound.getId(), dto.getId());
        assertEquals(match.getId(), dto.getMatchId());
        assertEquals(matchRound.getStartTime(), dto.getStartTime());
        assertEquals(matchRound.getEndTime(), dto.getEndTime());
    }

    @Test
    void shouldMapToEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        MatchRoundDTO dto = MatchRoundDTO.builder()
                .id(1L)
                .matchId(1L)
                .startTime(now)
                .endTime(now.plusHours(1))
                .build();

        // When
        MatchRound entity = mapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getStartTime(), entity.getStartTime());
        assertEquals(dto.getEndTime(), entity.getEndTime());
        // Match should be null as it's managed by service layer
        assertNull(entity.getMatch());
    }

    @Test
    void shouldReturnNullForNullInput() {
        assertNull(mapper.toDto(null));
        assertNull(mapper.toEntity(null));
    }
}
