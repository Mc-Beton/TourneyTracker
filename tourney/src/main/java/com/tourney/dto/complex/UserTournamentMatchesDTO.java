package com.tourney.dto.complex;

import com.tourney.dto.matches.MatchDetailsDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserTournamentMatchesDTO {
    private Long userId;
    private String userName;
    private List<MatchDetailsDTO> matches;
}

