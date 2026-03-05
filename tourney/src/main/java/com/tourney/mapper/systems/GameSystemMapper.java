package com.tourney.mapper.systems;

import com.tourney.domain.systems.GameSystem;
import com.tourney.dto.systems.GameSystemDTO;
import org.springframework.stereotype.Component;

@Component
public class GameSystemMapper {

    public GameSystemDTO toDto(GameSystem gameSystem) {
        if (gameSystem == null) {
            return null;
        }
        GameSystemDTO dto = new GameSystemDTO();
        dto.setId(gameSystem.getId());
        dto.setName(gameSystem.getName());
        return dto;
    }
}
