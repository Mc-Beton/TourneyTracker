package com.tourney.service.team;

import com.common.domain.User;
import com.tourney.domain.team.Team;
import com.tourney.domain.team.TeamMember;
import com.tourney.domain.team.TeamMemberStatus;
import com.tourney.domain.systems.GameSystem;
import com.tourney.dto.team.CreateTeamRequest;
import com.tourney.dto.team.TeamDTO;
import com.tourney.dto.team.TeamMemberDTO;
import com.tourney.dto.team.UpdateTeamRequest;
import com.tourney.repository.team.TeamRepository;
import com.tourney.repository.team.TeamMemberRepository;
import com.tourney.repository.systems.GameSystemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final GameSystemRepository gameSystemRepository;

    @Transactional
    public TeamDTO createTeam(CreateTeamRequest request, User owner) {
        GameSystem gameSystem = gameSystemRepository.findById(request.getGameSystemId())
                .orElseThrow(() -> new EntityNotFoundException("Game System not found"));

        if (teamRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Team name already exists");
        }

        // Check if user is already a member of another team for this system
        Optional<TeamMember> existingMembership = teamMemberRepository.findActiveMembership(owner, gameSystem, TeamMemberStatus.ACTIVE);
        if (existingMembership.isPresent()) {
            throw new IllegalStateException("User is already a member of a team for this game system");
        }

        Team team = Team.builder()
                .name(request.getName())
                .abbreviation(request.getAbbreviation())
                .city(request.getCity())
                .description(request.getDescription())
                .gameSystem(gameSystem)
                .owner(owner)
                .build();
        
        team = teamRepository.save(team);

        // Add owner as a member
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(owner)
                .status(TeamMemberStatus.ACTIVE)
                .build();
        teamMemberRepository.save(member);

        return mapToDTO(team, owner);
    }

    @Transactional(readOnly = true)
    public List<TeamDTO> getMyTeams(User user) {
        List<TeamMember> memberships = teamMemberRepository.findByUser(user);
        return memberships.stream()
                .map(m -> mapToDTO(m.getTeam(), user))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TeamDTO> getAllTeams(User user) {
        return teamRepository.findAll().stream()
                .map(team -> mapToDTO(team, user))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamDTO getTeam(Long teamId, User user) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        return mapToDTO(team, user);
    }

    @Transactional
    public void joinTeam(Long teamId, User user) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        // Check if user is already a member of ANY team for this system
        Optional<TeamMember> existingMembership = teamMemberRepository.findActiveMembership(user, team.getGameSystem(), TeamMemberStatus.ACTIVE);
        if (existingMembership.isPresent()) {
            throw new IllegalStateException("User is already a member of a team for this game system");
        }

        // Check if already requested (PENDING) or member (ACTIVE) of THIS team specifically
        Optional<TeamMember> membership = teamMemberRepository.findByTeamAndUser(team, user);
        if (membership.isPresent()) {
             if (membership.get().getStatus() == TeamMemberStatus.ACTIVE) {
                 throw new IllegalStateException("User is already a member of this team");
             } else if (membership.get().getStatus() == TeamMemberStatus.PENDING) {
                 throw new IllegalStateException("User has already requested to join this team");
             }
        }

        TeamMember newMember = TeamMember.builder()
                .team(team)
                .user(user)
                .status(TeamMemberStatus.PENDING)
                .build();
        teamMemberRepository.save(newMember);
    }

    @Transactional
    public void acceptMember(Long teamId, Long memberId, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getOwner().getId().equals(currentUser.getId())) {
             throw new IllegalStateException("Only the owner can accept members");
        }

        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member request not found"));

        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Member does not belong to this team");
        }

        // Double check if user joined another team meanwhile
        Optional<TeamMember> existing = teamMemberRepository.findActiveMembership(member.getUser(), team.getGameSystem(), TeamMemberStatus.ACTIVE);
        if (existing.isPresent()) {
            teamMemberRepository.delete(member); // Remove pending request as they joined another team
            throw new IllegalStateException("User has already joined another team for this system");
        }

        member.setStatus(TeamMemberStatus.ACTIVE);
        teamMemberRepository.save(member);
    }
    
    @Transactional
    public void kickMember(Long teamId, Long memberId, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getOwner().getId().equals(currentUser.getId())) {
             throw new IllegalStateException("Only the owner can kick members");
        }
        
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        if (member.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Owner cannot kick themselves. Transfer ownership or delete team.");
        }

        teamMemberRepository.delete(member);
    }

    @Transactional
    public TeamDTO updateTeam(Long teamId, UpdateTeamRequest request, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getOwner().getId().equals(currentUser.getId())) {
             throw new IllegalStateException("Only the owner can update the team");
        }

        if (request.getName() != null && !request.getName().isEmpty() && !request.getName().equals(team.getName())) {
             if (teamRepository.findByName(request.getName()).isPresent()) {
                 throw new IllegalArgumentException("Team name already exists");
             }
             team.setName(request.getName());
        }

        if (request.getAbbreviation() != null) team.setAbbreviation(request.getAbbreviation());
        if (request.getCity() != null) team.setCity(request.getCity());
        if (request.getDescription() != null) team.setDescription(request.getDescription());
        
        team = teamRepository.save(team);
        return mapToDTO(team, currentUser);
    }

    @Transactional
    public void transferOwnership(Long teamId, Long newOwnerMemberId, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        if (!team.getOwner().getId().equals(currentUser.getId())) {
             throw new IllegalStateException("Only the owner can transfer ownership");
        }

        TeamMember member = teamMemberRepository.findById(newOwnerMemberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        if (!member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Member does not belong to this team");
        }
        
        if (member.getStatus() != TeamMemberStatus.ACTIVE) {
            throw new IllegalStateException("New owner must be an active member");
        }

        team.setOwner(member.getUser());
        teamRepository.save(team);
    }

    @Transactional
    public void leaveTeam(Long teamId, User user) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        
        if (team.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("Owner cannot leave the team. Transfer ownership first.");
        }

        TeamMember member = teamMemberRepository.findByTeamAndUser(team, user)
                .orElseThrow(() -> new EntityNotFoundException("You are not a member of this team"));

        teamMemberRepository.delete(member);
    }
    
    @Transactional(readOnly = true)
    public List<TeamMemberDTO> getTeamMembers(Long teamId) {
         Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
         
         return teamMemberRepository.findByTeam(team).stream()
                 .map(this::mapMemberToDTO)
                 .collect(Collectors.toList());
    }

    private TeamDTO mapToDTO(Team team, User currentUser) {
        boolean isOwner = currentUser != null && team.getOwner().getId().equals(currentUser.getId());
        boolean isMember = false; 
        
        if (currentUser != null) {
            Optional<TeamMember> member = teamMemberRepository.findByTeamAndUser(team, currentUser);
            if (member.isPresent() && member.get().getStatus() == TeamMemberStatus.ACTIVE) {
                isMember = true;
            }
        }
        
        // This is inefficient (N+1), but simple for now. Can be optimized with a custom query.
        int count = teamMemberRepository.findByTeamAndStatus(team, TeamMemberStatus.ACTIVE).size();

        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .abbreviation(team.getAbbreviation())
                .city(team.getCity())
                .description(team.getDescription())
                .ownerId(team.getOwner().getId())
                .ownerName(team.getOwner().getName()) // Assuming User has getName()
                .gameSystemId(team.getGameSystem().getId())
                .gameSystemName(team.getGameSystem().getName())
                .createdAt(team.getCreatedAt())
                .memberCount(count)
                .isOwner(isOwner)
                .isMember(isMember)
                .build();
    }
    
    private TeamMemberDTO mapMemberToDTO(TeamMember member) {
        return TeamMemberDTO.builder()
                .id(member.getId())
                .teamId(member.getTeam().getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .status(member.getStatus())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
