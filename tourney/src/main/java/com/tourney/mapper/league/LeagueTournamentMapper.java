package com.tourney.mapper.league;

import com.tourney.domain.league.LeagueTournament;
import com.tourney.dto.league.LeagueTournamentDTO;
import com.tourney.mapper.tournament.TournamentMapper;
import com.tourney.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LeagueTournamentMapper {

    private final UserMapper userMapper;
    private final TournamentMapper tournamentMapper;

    @Autowired
    public LeagueTournamentMapper(UserMapper userMapper, TournamentMapper tournamentMapper) {
        this.userMapper = userMapper;
        this.tournamentMapper = tournamentMapper;
    }

    public LeagueTournamentDTO toDto(LeagueTournament leagueTournament) {
        if (leagueTournament == null) {
            return null;
        }
        LeagueTournamentDTO dto = new LeagueTournamentDTO();
        dto.setId(leagueTournament.getId());
        dto.setLeagueId(leagueTournament.getLeague().getId());
        dto.setTournament(tournamentMapper.toDto(leagueTournament.getTournament()));
        dto.setSubmittedBy(userMapper.toDto(leagueTournament.getSubmittedBy()));
        dto.setStatus(leagueTournament.getStatus());
        dto.setSubmitDate(leagueTournament.getSubmittedAt());
        dto.setProcessedDate(leagueTournament.getProcessedAt());
        dto.setRejectionReason(leagueTournament.getRejectionReason());
        return dto;
    }
}
