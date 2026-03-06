package com.tourney.mapper.league;

import com.tourney.domain.league.League;
import com.tourney.dto.league.LeagueDTO;
import com.tourney.mapper.systems.GameSystemMapper;
import com.tourney.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LeagueMapper {

    private final UserMapper userMapper;
    private final GameSystemMapper gameSystemMapper;

    @Autowired
    public LeagueMapper(UserMapper userMapper, GameSystemMapper gameSystemMapper) {
        this.userMapper = userMapper;
        this.gameSystemMapper = gameSystemMapper;
    }

    public LeagueDTO toDto(League league) {
        if (league == null) {
            return null;
        }
        LeagueDTO dto = new LeagueDTO();
        dto.setId(league.getId());
        dto.setName(league.getName());
        dto.setDescription(league.getDescription());
        if (league.getStatus() != null) {
            dto.setStatus(league.getStatus().name());
        }
        dto.setGameSystem(gameSystemMapper.toDto(league.getGameSystem()));
        dto.setOwner(userMapper.toDto(league.getOwner()));
        dto.setStartDate(league.getStartDate());
        dto.setEndDate(league.getEndDate());
        dto.setAutoAcceptGames(league.isAutoAcceptGames());
        dto.setAutoAcceptTournaments(league.isAutoAcceptTournaments());
        dto.setPointsWin(league.getPointsWin());
        dto.setPointsDraw(league.getPointsDraw());
        dto.setPointsLoss(league.getPointsLoss());
        dto.setPointsParticipation(league.getPointsParticipation());
        dto.setPointsPerParticipant(league.getPointsPerParticipant());
        dto.setMemberCount(league.getMemberCount());
        return dto;
    }
}
