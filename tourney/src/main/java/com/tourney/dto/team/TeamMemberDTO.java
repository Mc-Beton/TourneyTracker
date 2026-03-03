package com.tourney.dto.team;

import com.tourney.domain.team.TeamMemberStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TeamMemberDTO {
    private Long id;
    private Long teamId;
    private Long userId;
    private String userName;
    private TeamMemberStatus status;
    private LocalDateTime joinedAt;
}
