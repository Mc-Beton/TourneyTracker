package com.tourney.mapper.league;

import com.tourney.domain.tournament.Tournament;
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

    // Old method removed - LeagueTournament entity is deprecated
    // Use toTournamentDto(Tournament) instead
    
    /**
     * Map Tournament directly to LeagueTournamentDTO (new relationship model)
     */
    public LeagueTournamentDTO toTournamentDto(Tournament tournament) {
        if (tournament == null) {
            return null;
        }
        LeagueTournamentDTO dto = new LeagueTournamentDTO();
        dto.setId(tournament.getId());
        dto.setLeagueId(tournament.getLeague() != null ? tournament.getLeague().getId() : null);
        dto.setTournament(tournamentMapper.toDto(tournament));
        dto.setSubmittedBy(userMapper.toDto(tournament.getOrganizer()));
        dto.setStatus(tournament.getStatus());
        dto.setSubmitDate(null); // No longer tracked separately
        dto.setProcessedDate(null); // No longer tracked separately
        dto.setRejectionReason(null);
        return dto;
    }
}
