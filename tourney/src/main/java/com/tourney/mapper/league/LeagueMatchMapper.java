package com.tourney.mapper.league;

import com.tourney.domain.league.LeagueMatch;
import com.tourney.dto.league.LeagueMatchDTO;
import com.tourney.domain.games.SingleMatch;
import com.tourney.mapper.match.SingleMatchMapper;
import com.tourney.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LeagueMatchMapper {

    private final UserMapper userMapper;
    private final SingleMatchMapper singleMatchMapper;

    @Autowired
    public LeagueMatchMapper(UserMapper userMapper, SingleMatchMapper singleMatchMapper) {
        this.userMapper = userMapper;
        this.singleMatchMapper = singleMatchMapper;
    }

    public LeagueMatchDTO toDto(LeagueMatch leagueMatch) {
        if (leagueMatch == null) {
            return null;
        }
        LeagueMatchDTO dto = new LeagueMatchDTO();
        dto.setId(leagueMatch.getId());
        dto.setLeagueId(leagueMatch.getLeague().getId());
        // LeagueMatch has getMatch() which returns SingleMatch (Entity)
        dto.setMatch(singleMatchMapper.toDto(leagueMatch.getMatch()));
        dto.setSubmittedBy(userMapper.toDto(leagueMatch.getSubmittedBy()));
        dto.setStatus(leagueMatch.getMatch().getStatus());
        dto.setSubmitDate(leagueMatch.getSubmittedAt());
        dto.setProcessedDate(leagueMatch.getProcessedAt());
        dto.setRejectionReason(leagueMatch.getRejectionReason());
        return dto;
    }
}
