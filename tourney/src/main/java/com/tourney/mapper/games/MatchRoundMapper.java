package com.tourney.mapper.games;

import com.tourney.domain.games.MatchRound;
import com.tourney.dto.games.MatchRoundDTO;
import org.springframework.stereotype.Component;

@Component
public class MatchRoundMapper {

    public MatchRoundDTO toDto(MatchRound matchRound) {
        if (matchRound == null) {
            return null;
        }

        return MatchRoundDTO.builder()
                .id(matchRound.getId())
                .matchId(matchRound.getMatch() != null ? matchRound.getMatch().getId() : null)
                .startTime(matchRound.getStartTime())
                .endTime(matchRound.getEndTime())
                .build();
    }

    public MatchRound toEntity(MatchRoundDTO dto) {
        if (dto == null) {
            return null;
        }

        MatchRound matchRound = new MatchRound();
        matchRound.setId(dto.getId());
        matchRound.setStartTime(dto.getStartTime());
        matchRound.setEndTime(dto.getEndTime());
        // Note: match association should be set by the service layer
        return matchRound;
    }

    public void updateEntity(MatchRound entity, MatchRoundDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        // Note: match association should be managed by the service layer
    }
}