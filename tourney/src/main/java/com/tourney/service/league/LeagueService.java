package com.tourney.service.league;

import com.common.domain.User;
import com.tourney.domain.league.*;
import com.tourney.domain.games.Match;
import com.tourney.domain.games.SingleMatch;
import com.tourney.domain.games.MatchStatus;
import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.tournament.TournamentStatus;
import com.tourney.dto.league.*;
import com.tourney.dto.tournament.ParticipantStatsDTO;
import com.tourney.mapper.league.LeagueMapper;
import com.tourney.mapper.league.LeagueMemberMapper;
import com.tourney.mapper.league.LeagueMatchMapper;
import com.tourney.mapper.league.LeagueTournamentMapper;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.league.LeagueMatchRepository;
import com.tourney.repository.league.LeagueMemberRepository;
import com.tourney.repository.league.LeagueRepository;
// import com.tourney.repository.league.LeagueTournamentRepository; // Deprecated - using Tournament.league instead
import com.tourney.repository.league.LeagueChallengeRepository;
import com.tourney.repository.systems.GameSystemRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.repository.user.UserRepository;
import com.tourney.service.tournament.TournamentStatsService;
import com.tourney.service.match.SingleMatchService;
import com.tourney.dto.matches.CreateSingleMatchDTO;
import com.tourney.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeagueService {
    
    private final LeagueRepository leagueRepository;
    private final LeagueMemberRepository leagueMemberRepository;
    // private final LeagueTournamentRepository leagueTournamentRepository; // Deprecated - using Tournament.league instead
    private final LeagueMatchRepository leagueMatchRepository;
    private final LeagueChallengeRepository leagueChallengeRepository;
    private final GameSystemRepository gameSystemRepository;
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final TournamentStatsService tournamentStatsService;
    private final SingleMatchService singleMatchService;
    private final LeagueMapper leagueMapper;
    private final LeagueMemberMapper leagueMemberMapper;
    private final LeagueMatchMapper leagueMatchMapper;
    private final LeagueTournamentMapper leagueTournamentMapper;
    private final NotificationService notificationService;

    @Transactional
    public LeagueDTO createLeague(CreateLeagueDTO createDto, User owner) {
        GameSystem gameSystem = gameSystemRepository.findById(createDto.getGameSystemId())
                .orElseThrow(() -> new IllegalArgumentException("Game System not found"));
        
        League league = League.builder()
                .name(createDto.getName())
                .description(createDto.getDescription())
                .gameSystem(gameSystem)
                .owner(owner)
                .startDate(createDto.getStartDate())
                .endDate(createDto.getEndDate())
                .autoAcceptGames(createDto.isAutoAcceptGames())
                .autoAcceptTournaments(createDto.isAutoAcceptTournaments())
                .pointsWin(createDto.getPointsWin())
                .pointsDraw(createDto.getPointsDraw())
                .pointsLoss(createDto.getPointsLoss())
                .pointsParticipation(createDto.getPointsParticipation())
                .pointsPerParticipant(createDto.getPointsPerParticipant())
                .status(LeagueStatus.DRAFT)
                .build();
        
        league = leagueRepository.save(league);
        
        LeagueMember ownerMember = LeagueMember.builder()
                .league(league)
                .user(owner)
                .status(LeagueMemberStatus.APPROVED)
                .points(0)
                .build();
        leagueMemberRepository.save(ownerMember);
        
        return leagueMapper.toDto(league);
    }

    @Transactional
    public LeagueDTO updateLeague(Long leagueId, UpdateLeagueDTO dto, User owner) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        if (!league.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only owner can update league");
        }

        if (dto.getName() != null) league.setName(dto.getName());
        if (dto.getDescription() != null) league.setDescription(dto.getDescription());
        if (dto.getStartDate() != null) league.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) league.setEndDate(dto.getEndDate());
        league.setAutoAcceptGames(dto.isAutoAcceptGames());
        league.setAutoAcceptTournaments(dto.isAutoAcceptTournaments());
        league.setPointsWin(dto.getPointsWin());
        league.setPointsDraw(dto.getPointsDraw());
        league.setPointsLoss(dto.getPointsLoss());
        league.setPointsParticipation(dto.getPointsParticipation());
        league.setPointsPerParticipant(dto.getPointsPerParticipant());

        return leagueMapper.toDto(leagueRepository.save(league));
    }

    @Transactional
    public void deleteLeague(Long leagueId, User owner) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        if (!league.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only owner can delete league");
        }

        // Can only delete leagues in DRAFT status
        if (league.getStatus() != LeagueStatus.DRAFT) {
            throw new IllegalArgumentException("Can only delete leagues in DRAFT status");
        }

        // Check if there are any matches associated with this league
        long matchCount = leagueMatchRepository.countByLeague(league);
        if (matchCount > 0) {
            throw new IllegalArgumentException("Cannot delete league with existing matches");
        }

        // Check if only owner is a member
        List<LeagueMember> members = leagueMemberRepository.findByLeague(league);
        if (members.size() > 1) {
            throw new IllegalArgumentException("Cannot delete league with other members. Only owner should be present.");
        }

        // Remove owner from members first
        leagueMemberRepository.deleteAll(members);
        
        // Delete any pending challenges
        leagueChallengeRepository.deleteByLeague(league);
        
        // Now delete the league
        leagueRepository.delete(league);
    }

    @Transactional
    public LeagueDTO setLeagueStatus(Long leagueId, String status, User owner) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        if (!league.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only owner can update league status");
        }

        try {
            LeagueStatus newStatus = LeagueStatus.valueOf(status.toUpperCase());
            league.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        return leagueMapper.toDto(leagueRepository.save(league));
    }

    @Transactional
    public void leaveLeague(Long leagueId, User user) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        LeagueMember member = leagueMemberRepository.findByLeagueAndUser(league, user)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this league"));
        
        // Prevent owner from leaving (or enforce transfer ownership first - for now just prevent)
        if (league.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Owner cannot leave the league. Delete the league instead.");
        }

        leagueMemberRepository.delete(member);
    }


    @Transactional(readOnly = true)
    public Page<LeagueDTO> listLeagues(Pageable pageable) {
        return leagueRepository.findByStatusNot(LeagueStatus.ARCHIVED, pageable).map(leagueMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<LeagueDTO> listJoinedLeagues(Long userId, Pageable pageable) {
        return leagueRepository.findJoinedLeagues(userId, pageable).map(leagueMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<LeagueDTO> listAvailableLeagues(Long userId, Pageable pageable) {
        return leagueRepository.findAvailableLeagues(userId, pageable).map(leagueMapper::toDto);
    }

    @Transactional(readOnly = true)
    public LeagueDTO getLeague(Long id) {
        return leagueRepository.findById(id)
                .map(leagueMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
    }

    @Transactional(readOnly = true)
    public List<LeagueMemberDTO> getLeagueMembers(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        // Filter only approved members for general view? Probably yes.
        return leagueMemberRepository.findByLeagueOrderByPointsDesc(league).stream()
                .filter(m -> m.getStatus() == LeagueMemberStatus.APPROVED)
                .map(leagueMemberMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeagueMemberDTO> getPendingMembers(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        return leagueMemberRepository.findByLeagueAndStatus(league, LeagueMemberStatus.PENDING, Pageable.unpaged()).stream()
                .map(leagueMemberMapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<LeagueMatchDTO> getLeagueMatches(Long leagueId, Pageable pageable) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        return leagueMatchRepository.findByLeague(league, pageable)
                .map(leagueMatchMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<LeagueTournamentDTO> getLeagueTournaments(Long leagueId, Pageable pageable) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        // Query tournaments directly by league relationship
        return tournamentRepository.findByLeague(league, pageable)
                .map(leagueTournamentMapper::toTournamentDto);
    }
    
    @Transactional(readOnly = true)
    public Page<LeagueMatchDTO> getPendingMatches(Long leagueId, Pageable pageable) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        return leagueMatchRepository.findByLeagueAndMatchStatusWithData(league, MatchStatus.PENDING, pageable)
                .map(leagueMatchMapper::toDto);
    }

    @Deprecated // Replaced by direct Tournament.league relationship
    @Transactional(readOnly = true)
    public Page<LeagueTournamentDTO> getPendingTournaments(Long leagueId, Pageable pageable) {
        // TODO: Remove this method - tournament submissions are handled differently now
        throw new UnsupportedOperationException("Tournament submission workflow has been replaced - tournaments are created with league directly");
    }

    @Transactional
    public void joinLeague(Long leagueId, User user) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        
        if (leagueMemberRepository.findByLeagueAndUser(league, user).isPresent()) {
            throw new IllegalArgumentException("User is already a member of this league");
        }

        LeagueMember member = LeagueMember.builder()
                .league(league)
                .user(user)
                .status(LeagueMemberStatus.PENDING) 
                .points(0)
                .build();
        
        leagueMemberRepository.save(member);
    }

    @Transactional
    public void approveMember(Long leagueId, Long userId, User approver) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        
        if (!league.getOwner().getId().equals(approver.getId())) {
             throw new IllegalArgumentException("Only owner can approve members");
        }
        
        User user = userRepository.findById(userId)
             .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LeagueMember member = leagueMemberRepository.findByLeagueAndUser(league, user)
             .orElseThrow(() -> new IllegalArgumentException("Member request not found"));
             
        member.setStatus(LeagueMemberStatus.APPROVED);
        leagueMemberRepository.save(member);
    }
    
    @Transactional
    public void syncLeagueMatchStatus(Match match) {
        if (!(match instanceof SingleMatch)) return;
        SingleMatch singleMatch = (SingleMatch) match;
        
        Optional<LeagueMatch> opt = leagueMatchRepository.findByMatch(singleMatch);
        if (opt.isPresent()) {
            LeagueMatch lm = opt.get();
             if (singleMatch.getStatus() == MatchStatus.COMPLETED) {
                 processMatchPoints(lm);
             }
        }
    }

    @Transactional
    public void submitMatch(Long leagueId, Long matchId, User submittor) {
        League league = leagueRepository.findById(leagueId)
             .orElseThrow(() -> new IllegalArgumentException("League not found"));
             
        Match match = matchRepository.findById(matchId)
             .orElseThrow(() -> new IllegalArgumentException("Match not found"));
             
        if (!(match instanceof SingleMatch)) {
             throw new IllegalArgumentException("Only single matches can be submitted directly");
        }
        
        SingleMatch singleMatch = (SingleMatch) match;
        
        // Validate that completed matches have results
        if (singleMatch.getStatus() == MatchStatus.COMPLETED && singleMatch.getMatchResult() == null) {
            throw new IllegalArgumentException("Cannot submit a completed match without results. Please finish the match properly first.");
        }
        
        // Removed explicit check for COMPLETED status to allow submitting pending matches for tracking
        // But if it IS completed, we process points immediately.

        Optional<LeagueMatch> existingMatch = leagueMatchRepository.findByLeagueAndMatch(league, singleMatch);
        LeagueMatch leagueMatch;

        if (existingMatch.isPresent()) {
            leagueMatch = existingMatch.get();
            if (leagueMatch.getProcessedAt() != null) {
                // If processed, we don't re-submit. (Or maybe we update submission info?)
                // For now, keep existing behavior: prevent re-submission if processed.
                throw new IllegalArgumentException("Match already submitted and processed");
            }
            // Update submitter to the person actually submitting the result
            leagueMatch.setSubmittedBy(submittor);
            
            // Status synced from Match entity
        } else {
            leagueMatch = LeagueMatch.builder()
                  .league(league)
                  .match(singleMatch)
                  .submittedBy(submittor)
                  .status(singleMatch.getStatus())
                  .build();
        }
              
        leagueMatch = leagueMatchRepository.save(leagueMatch);
        
        if (singleMatch.getStatus() == MatchStatus.COMPLETED) {
            processMatchPoints(leagueMatch);
        }
    }
    
    @Transactional
    @Deprecated // Replaced by direct Tournament.league relationship
    public void submitTournament(Long leagueId, Long tournamentId, User submittor) {
        // TODO: Remove this method - tournaments are now created with league_id directly
        throw new UnsupportedOperationException("Tournament submission workflow has been replaced - set league during tournament creation");
    }

    @Transactional
    public void approveMatch(Long leagueMatchId, User approver) {
        LeagueMatch leagueMatch = leagueMatchRepository.findById(leagueMatchId)
             .orElseThrow(() -> new IllegalArgumentException("League Match request not found"));
             
        if (!leagueMatch.getLeague().getOwner().getId().equals(approver.getId())) {
             throw new IllegalArgumentException("Only owner can approve matches");
        }

        if (leagueMatch.getMatch().getStatus() != MatchStatus.PENDING) {
             throw new IllegalArgumentException("Request is not pending");
        }
        
        SingleMatch match = leagueMatch.getMatch();
        
        // Validate that the match has results before approving
        if (match.getMatchResult() == null) {
            throw new IllegalArgumentException("Cannot approve match without results. Match must be played and finished first.");
        }
        
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);
        
        processMatchPoints(leagueMatch);
    }
    
    @Transactional
    @Deprecated // Replaced by direct Tournament.league relationship
    public void approveTournament(Long leagueTournamentId, User approver) {
        // TODO: Remove this method - tournaments are now created with league_id directly, no approval needed
        throw new UnsupportedOperationException("Tournament approval workflow has been replaced - tournaments are accepted at creation");
    }

    private void processMatchPoints(LeagueMatch leagueMatch) {
        if (leagueMatch.getProcessedAt() != null) return;
        
        League league = leagueMatch.getLeague();
        SingleMatch match = leagueMatch.getMatch();
        
        if (match.getMatchResult() == null) {
            log.warn("Match {} has no result, skipping points processing", match.getId());
            return;
        }

        Long winnerId = match.getMatchResult().getWinnerId();
        
        processPlayerMatchPoints(league, match.getPlayer1(), winnerId, match);
        processPlayerMatchPoints(league, match.getPlayer2(), winnerId, match);
        
        leagueMatch.setProcessedAt(LocalDateTime.now());
        leagueMatchRepository.save(leagueMatch);
    }

    private void processPlayerMatchPoints(League league, User player, Long winnerId, SingleMatch match) {
        Optional<LeagueMember> memberOpt = leagueMemberRepository.findByLeagueAndUser(league, player);
        if (memberOpt.isEmpty()) return;
        
        LeagueMember member = memberOpt.get();
        
        int pointsToAdd = 0;
        
        if (winnerId == null) {
            // Draw
            pointsToAdd += league.getPointsDraw();
            member.setDraws(member.getDraws() + 1);
        } else if (winnerId.equals(player.getId())) {
            // Win
            pointsToAdd += league.getPointsWin();
            member.setWins(member.getWins() + 1);
        } else {
            // Loss
            pointsToAdd += league.getPointsLoss();
            member.setLosses(member.getLosses() + 1);
        }
        
        member.setPoints(member.getPoints() + pointsToAdd);
        member.setMatchesPlayed(member.getMatchesPlayed() + 1);
        
        if (match.getMatchResult().getPlayerScores().containsKey(player.getId())) {
             // Handle points logic here if needed
        }
        
        leagueMemberRepository.save(member);
    }

    /**
     * Process tournament points for league members
     * Called when a tournament with a league_id is completed
     */
    @Transactional
    public void processTournamentPoints(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
        
        if (tournament.getLeague() == null) {
            throw new IllegalArgumentException("Tournament is not associated with a league");
        }
        
        if (tournament.getStatus() != TournamentStatus.COMPLETED) {
            throw new IllegalArgumentException("Tournament is not completed");
        }
        
        League league = tournament.getLeague();
        
        // Calculate participant stats and rankings
        List<ParticipantStatsDTO> stats = tournamentStatsService.calculateParticipantStats(tournament);
        
        int totalParticipants = stats.size();
        for (int i = 0; i < totalParticipants; i++) {
            ParticipantStatsDTO stat = stats.get(i);
            int rank = i + 1;
            
            User user = userRepository.findById(stat.getUserId()).orElse(null);
            if (user == null) continue;
            
            Optional<LeagueMember> memberOpt = leagueMemberRepository.findByLeagueAndUser(league, user);
            if (memberOpt.isPresent()) {
                LeagueMember member = memberOpt.get();
                
                // Only award points to APPROVED league members
                if (member.getStatus() != LeagueMemberStatus.APPROVED) {
                    log.debug("Skipping points for user {} - not an approved league member (status: {})", 
                        user.getId(), member.getStatus());
                    continue;
                }
                
                // Base participation points
                int pointsToAdd = league.getPointsParticipation();
                
                // Ranking-based points (better finish = more points)
                int rankPoints = (totalParticipants - rank + 1) * league.getPointsPerParticipant();
                pointsToAdd += rankPoints;
                
                member.setPoints(member.getPoints() + pointsToAdd);
                member.setTournamentsPlayed(member.getTournamentsPlayed() + 1);
                
                if (rank == 1) {
                    member.setTournamentWins(member.getTournamentWins() + 1);
                }
                
                leagueMemberRepository.save(member);
                log.info("Awarded {} points to user {} in league {} (rank: {})", 
                    pointsToAdd, user.getId(), league.getId(), rank);
            }
        }
    }

    /**
     * Process tournament points with authorization
     * Called from the REST endpoint
     */
    @Transactional
    public void processTournamentPoints(Long leagueId, Long tournamentId, User user) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        
        // Only league owner can manually process tournament points
        if (!league.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only the league owner can process tournament points");
        }
        
        // Delegate to the main processing method
        processTournamentPoints(tournamentId);
    }

    @Transactional
    public void createChallenge(Long leagueId, Long challengerId, CreateChallengeDTO dto) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        if (!league.getStatus().equals(LeagueStatus.ACTIVE)) {
            throw new IllegalArgumentException("League is not active");
        }

        User challenger = userRepository.findById(challengerId)
                .orElseThrow(() -> new IllegalArgumentException("Challenger not found"));
        User challenged = userRepository.findById(dto.getOpponentId())
                .orElseThrow(() -> new IllegalArgumentException("Challenged user not found"));

        // Check if both are members
        if (leagueMemberRepository.findByLeagueAndUser(league, challenger).isEmpty() ||
            leagueMemberRepository.findByLeagueAndUser(league, challenged).isEmpty()) {
            throw new IllegalArgumentException("Both users must be members of the league");
        }
        
        LeagueChallenge challenge = LeagueChallenge.builder()
                .league(league)
                .challenger(challenger)
                .challenged(challenged)
                .status(MatchStatus.PENDING)
                .scheduledTime(dto.getScheduledTime())
                .message(dto.getMessage())
                .createdDate(LocalDateTime.now())
                .build();
        
        leagueChallengeRepository.save(challenge);

        notificationService.notifyLeagueChallengeReceived(
                challenged.getId(), 
                challenger.getId(), 
                challenger.getName(), 
                league.getId(), 
                league.getName()
        );
    }

    @Transactional
    public void respondToChallenge(Long challengeId, Long userId, boolean accept) {
        LeagueChallenge challenge = leagueChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        if (!challenge.getChallenged().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the challenged user can respond");
        }

        if (challenge.getStatus() != MatchStatus.PENDING) {
            throw new IllegalArgumentException("Challenge is not pending");
        }

        if (accept) {
            // Challenge is now scheduled
            challenge.setStatus(MatchStatus.SCHEDULED);
            
            // Create Single Match
            CreateSingleMatchDTO matchDto = CreateSingleMatchDTO.builder()
                    .gameSystemId(challenge.getLeague().getGameSystem().getId())
                    .player2Id(challenge.getChallenger().getId())
                    .build();
            
            // Note: createSingleMatch takes creatorId (player1), so challenged user becomes player1 (host)
            SingleMatch match = singleMatchService.createSingleMatch(matchDto, userId);
            
            // If league doesn't auto-accept games, match needs owner approval
            if (!challenge.getLeague().isAutoAcceptGames()) {
                match.setStatus(MatchStatus.PENDING);
                matchRepository.save(match);
            }
            
            // Link match to challenge
            challenge.setMatch(match);

            // Create LeagueMatch with same status as the match
            LeagueMatch leagueMatch = LeagueMatch.builder()
                    .league(challenge.getLeague())
                    .match(match)
                    .submittedBy(challenge.getChallenged())
                    .status(match.getStatus())
                    .build();
            leagueMatchRepository.save(leagueMatch);
            
            // Notify challenger that challenge was accepted
            notificationService.notifyLeagueChallengeAccepted(
                    challenge.getChallenger().getId(),
                    challenge.getChallenged().getId(),
                    challenge.getChallenged().getName(),
                    challenge.getLeague().getId(),
                    challenge.getLeague().getName()
            );
            
        } else {
            challenge.setStatus(MatchStatus.CANCELLED);
            
            // Notify challenger that challenge was rejected
            notificationService.notifyLeagueChallengeRejected(
                    challenge.getChallenger().getId(),
                    challenge.getChallenged().getId(),
                    challenge.getChallenged().getName(),
                    challenge.getLeague().getId(),
                    challenge.getLeague().getName()
            );
        }
        
        leagueChallengeRepository.save(challenge);
    }

    @Transactional(readOnly = true)
    public List<LeagueChallengeDTO> getMyChallenges(Long leagueId, Long userId) {
        return leagueChallengeRepository.findByChallengedIdAndStatus(userId, MatchStatus.PENDING).stream()
                .filter(c -> c.getLeague().getId().equals(leagueId))
                .map(this::toChallengeDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeagueChallengeDTO> getMyOutgoingChallenges(Long leagueId, Long userId) {
         return leagueChallengeRepository.findByChallengerIdAndStatus(userId, MatchStatus.PENDING).stream()
                .filter(c -> c.getLeague().getId().equals(leagueId))
                .map(this::toChallengeDto)
                .collect(Collectors.toList());
    }

    private LeagueChallengeDTO toChallengeDto(LeagueChallenge c) {
        return LeagueChallengeDTO.builder()
                .id(c.getId())
                .leagueId(c.getLeague().getId())
                .challengerId(c.getChallenger().getId())
                .challengerName(c.getChallenger().getName())
                .challengedId(c.getChallenged().getId())
                .challengedName(c.getChallenged().getName())
                .status(c.getStatus().name())
                .createdDate(c.getCreatedDate())
                .scheduledTime(c.getScheduledTime())
                .message(c.getMessage())
                .matchId(c.getMatch() != null ? c.getMatch().getId() : null)
                .build();
    }
}
