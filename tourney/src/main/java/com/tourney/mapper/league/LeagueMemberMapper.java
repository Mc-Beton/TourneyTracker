package com.tourney.mapper.league;

import com.tourney.domain.league.LeagueMember;
import com.tourney.dto.league.LeagueMemberDTO;
import com.tourney.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LeagueMemberMapper {

    private final UserMapper userMapper;

    @Autowired
    public LeagueMemberMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public LeagueMemberDTO toDto(LeagueMember member) {
        if (member == null) {
            return null;
        }
        LeagueMemberDTO dto = new LeagueMemberDTO();
        dto.setId(member.getId());
        dto.setLeagueId(member.getLeague().getId());
        dto.setUser(userMapper.toDto(member.getUser()));
        dto.setStatus(member.getStatus());
        dto.setPoints(member.getPoints());
        dto.setMatchesPlayed(member.getMatchesPlayed());
        dto.setWins(member.getWins());
        dto.setDraws(member.getDraws());
        dto.setLosses(member.getLosses());
        dto.setTournamentsPlayed(member.getTournamentsPlayed());
        dto.setTournamentWins(member.getTournamentWins());
        dto.setPointsScored(member.getPointsScored());
        dto.setHasPaid(member.isHasPaid());
        return dto;
    }
}
