package com.tourney.repository.team;

import com.common.domain.User;
import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.team.Team;
import com.tourney.domain.team.TeamMember;
import com.tourney.domain.team.TeamMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    
    List<TeamMember> findByTeam(Team team);
    
    List<TeamMember> findByUser(User user);

    Optional<TeamMember> findByTeamAndUser(Team team, User user);

    @Query("SELECT tm FROM TeamMember tm JOIN tm.team t WHERE tm.user = :user AND t.gameSystem = :gameSystem AND tm.status = :status")
    Optional<TeamMember> findActiveMembership(@Param("user") User user, @Param("gameSystem") GameSystem gameSystem, @Param("status") TeamMemberStatus status);

    List<TeamMember> findByTeamAndStatus(Team team, TeamMemberStatus status);
}
