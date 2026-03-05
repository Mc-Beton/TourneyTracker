package com.tourney.dto.league;

import com.tourney.domain.league.LeagueApprovalStatus;
import com.tourney.dto.tournament.TournamentResponseDTO;
import com.tourney.dto.user.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeagueTournamentDTO {

    private Long id;
    private Long leagueId;
    private TournamentResponseDTO tournament;
    private UserDTO submittedBy;
    private LeagueApprovalStatus status;
    private LocalDateTime submitDate;
    private LocalDateTime processedDate;
    private String rejectionReason;
}
