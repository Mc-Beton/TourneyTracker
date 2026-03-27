package com.tourney.controller.league;

import com.common.domain.User;
import com.common.security.UserPrincipal;
import com.tourney.dto.league.CreateLeagueDTO;
import com.tourney.dto.league.UpdateLeagueDTO;
import com.tourney.dto.league.LeagueDTO;
import com.tourney.dto.league.LeagueMemberDTO;
import com.tourney.dto.league.LeagueMatchDTO;
import com.tourney.dto.league.LeagueTournamentDTO;
import com.tourney.dto.league.LeagueChallengeDTO;
import com.tourney.repository.user.UserRepository;
import com.tourney.service.league.LeagueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<LeagueDTO> createLeague(@RequestBody CreateLeagueDTO createDto,
                                                  @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        return ResponseEntity.ok(leagueService.createLeague(createDto, user));
    }

    @GetMapping
    public ResponseEntity<Page<LeagueDTO>> listLeagues(Pageable pageable) {
        return ResponseEntity.ok(leagueService.listLeagues(pageable));
    }

    @GetMapping("/joined")
    public ResponseEntity<Page<LeagueDTO>> listJoinedLeagues(Pageable pageable, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        return ResponseEntity.ok(leagueService.listJoinedLeagues(user.getId(), pageable));
    }

    @GetMapping("/available")
    public ResponseEntity<Page<LeagueDTO>> listAvailableLeagues(Pageable pageable, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        return ResponseEntity.ok(leagueService.listAvailableLeagues(user.getId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueDTO> getLeague(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.getLeague(id));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<LeagueMemberDTO>> getLeagueMembers(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.getLeagueMembers(id));
    }

    @GetMapping("/{id}/members/pending")
    public ResponseEntity<List<LeagueMemberDTO>> getPendingMembers(@PathVariable Long id) {
        return ResponseEntity.ok(leagueService.getPendingMembers(id));
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<Page<LeagueMatchDTO>> getLeagueMatches(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(leagueService.getLeagueMatches(id, pageable));
    }

    @GetMapping("/{id}/tournaments")
    public ResponseEntity<Page<LeagueTournamentDTO>> getLeagueTournaments(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(leagueService.getLeagueTournaments(id, pageable));
    }

    @PostMapping("/{id}/tournaments/{tournamentId}/process-points")
    public ResponseEntity<Void> processTournamentPoints(@PathVariable Long id,
                                                        @PathVariable Long tournamentId,
                                                        @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.processTournamentPoints(id, tournamentId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/matches/pending")
    public ResponseEntity<Page<LeagueMatchDTO>> getPendingMatches(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(leagueService.getPendingMatches(id, pageable));
    }

    @GetMapping("/{id}/tournaments/pending")
    public ResponseEntity<Page<LeagueTournamentDTO>> getPendingTournaments(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(leagueService.getPendingTournaments(id, pageable));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinLeague(@PathVariable Long id,
                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.joinLeague(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/members/{userId}/approve")
    public ResponseEntity<Void> approveMember(@PathVariable Long id, @PathVariable Long userId,
                                              @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.approveMember(id, userId, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/matches/submit")
    public ResponseEntity<Void> submitMatch(@PathVariable Long id, @RequestParam Long matchId,
                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.submitMatch(id, matchId, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/tournaments/submit")
    public ResponseEntity<Void> submitTournament(@PathVariable Long id, @RequestParam Long tournamentId,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.submitTournament(id, tournamentId, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/matches/{matchRequestId}/approve")
    public ResponseEntity<Void> approveMatchRequest(@PathVariable Long matchRequestId,
                                                    @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.approveMatch(matchRequestId, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tournaments/{tournamentRequestId}/approve")
    public ResponseEntity<Void> approveTournamentRequest(@PathVariable Long tournamentRequestId,
                                                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.approveTournament(tournamentRequestId, user);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<LeagueDTO> updateLeague(@PathVariable Long id, @RequestBody UpdateLeagueDTO updateDto,
                                                  @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        return ResponseEntity.ok(leagueService.updateLeague(id, updateDto, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeague(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.deleteLeague(id, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LeagueDTO> setLeagueStatus(@PathVariable Long id, @RequestParam String status,
                                                     @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        return ResponseEntity.ok(leagueService.setLeagueStatus(id, status, user));
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveLeague(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.leaveLeague(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/challenges")
    public ResponseEntity<Void> createChallenge(@PathVariable Long id, @RequestBody com.tourney.dto.league.CreateChallengeDTO dto,
                                                @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.createChallenge(id, user.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/challenges/{challengeId}/respond")
    public ResponseEntity<Void> respondToChallenge(@PathVariable Long challengeId, @RequestParam boolean accept,
                                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        leagueService.respondToChallenge(challengeId, user.getId(), accept);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/challenges/my")
    public ResponseEntity<List<LeagueChallengeDTO>> getMyChallenges(@PathVariable Long id,
                                                                    @AuthenticationPrincipal UserPrincipal userPrincipal) {
         User user = getUser(userPrincipal);
         return ResponseEntity.ok(leagueService.getMyChallenges(id, user.getId()));
    }

    @GetMapping("/{id}/challenges/outgoing")
    public ResponseEntity<List<LeagueChallengeDTO>> getMyOutgoingChallenges(@PathVariable Long id,
                                                                            @AuthenticationPrincipal UserPrincipal userPrincipal) {
         User user = getUser(userPrincipal);
         return ResponseEntity.ok(leagueService.getMyOutgoingChallenges(id, user.getId()));
    }

    @PatchMapping("/{id}/members/{userId}/payment")
    public ResponseEntity<LeagueMemberDTO> togglePaymentStatus(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam boolean hasPaid,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        User owner = getUser(userPrincipal);
        LeagueMemberDTO result = leagueService.togglePaymentStatus(id, userId, hasPaid, owner.getId());
        return ResponseEntity.ok(result);
    }

    private User getUser(UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
