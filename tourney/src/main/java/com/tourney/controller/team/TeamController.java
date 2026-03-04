package com.tourney.controller.team;

import com.common.domain.User;
import com.common.security.UserPrincipal;
import com.tourney.dto.team.CreateTeamRequest;
import com.tourney.dto.team.TeamDTO;
import com.tourney.dto.team.TeamMemberDTO;
import com.tourney.dto.team.UpdateTeamRequest;
import com.tourney.service.team.TeamService;
import com.tourney.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final UserRepository userRepository;

    private User getUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<TeamDTO>> getAllTeams(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = getUser(currentUser);
        return ResponseEntity.ok(teamService.getAllTeams(user));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TeamDTO>> getMyTeams(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = getUser(currentUser);
        return ResponseEntity.ok(teamService.getMyTeams(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> getTeam(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = getUser(currentUser);
        return ResponseEntity.ok(teamService.getTeam(id, user));
    }

    @PostMapping
    public ResponseEntity<TeamDTO> createTeam(@Valid @RequestBody CreateTeamRequest request,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = getUser(currentUser);
        return new ResponseEntity<>(teamService.createTeam(request, user), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDTO> updateTeam(@PathVariable Long id, 
                                              @Valid @RequestBody UpdateTeamRequest request,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = getUser(currentUser);
        return ResponseEntity.ok(teamService.updateTeam(id, request, user));
    }
    
    @PutMapping("/{id}/owner/{memberId}")
    public ResponseEntity<Void> transferOwnership(@PathVariable Long id,
                                                  @PathVariable Long memberId,
                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = getUser(currentUser);
        teamService.transferOwnership(id, memberId, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinTeam(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = getUser(currentUser);
        teamService.joinTeam(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveTeam(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = getUser(currentUser);
        teamService.leaveTeam(id, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<TeamMemberDTO>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(teamService.getTeamMembers(id));
    }

    @PutMapping("/{id}/members/{memberId}/accept")
    public ResponseEntity<Void> acceptMember(@PathVariable Long id, @PathVariable Long memberId,
                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = getUser(currentUser);
        teamService.acceptMember(id, memberId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> kickMember(@PathVariable Long id, @PathVariable Long memberId,
                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = getUser(currentUser);
        teamService.kickMember(id, memberId, user);
        return ResponseEntity.ok().build();
    }
}
