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
import com.tourney.repository.league.LeagueTournamentRepository;
import com.tourney.repository.systems.GameSystemRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.repository.user.UserRepository;
import com.tourney.service.tournament.TournamentStatsService;
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
    private final LeagueTournamentRepository leagueTournamentRepository;
    private final LeagueMatchRepository leagueMatchRepository;
    private final GameSystemRepository gameSystemRepository;
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final TournamentStatsService tournamentStatsService;
    private final LeagueMapper leagueMapper;
    private final LeagueMemberMapper leagueMemberMapper;
    private final LeagueMatchMapper leagueMatchMapper;
    private final LeagueTournamentMapper leagueTournamentMapper;

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

    @Transactional(readOnly = true)
    public Page<LeagueDTO> listLeagues(Pageable pageable) {
        return leagueRepository.findAll(pageable).map(leagueMapper::toDto);
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
        return leagueMatchRepository.findByLeagueAndStatus(league, LeagueApprovalStatus.APPROVED, pageable)
                .map(leagueMatchMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<LeagueTournamentDTO> getLeagueTournaments(Long leagueId, Pageable pageable) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        return leagueTournamentRepository.findByLeagueAndStatus(league, LeagueApprovalStatus.APPROVED, pageable)
                .map(leagueTournamentMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Page<LeagueMatchDTO> getPendingMatches(Long leagueId, Pageable pageable) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        return leagueMatchRepository.findByLeagueAndStatus(league, LeagueApprovalStatus.PENDING, pageable)
                .map(leagueMatchMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<LeagueTournamentDTO> getPendingTournaments(Long leagueId, Pageable pageable) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("League not found"));
        return leagueTournamentRepository.findByLeagueAndStatus(league, LeagueApprovalStatus.PENDING, pageable)
                .map(leagueTournamentMapper::toDto);
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
    public void submitMatch(Long leagueId, Long matchId, User submittor) {
        League league = leagueRepository.findById(leagueId)
             .orElseThrow(() -> new IllegalArgumentException("League not found"));
             
        Match match = matchRepository.findById(matchId)
             .orElseThrow(() -> new IllegalArgumentException("Match not found"));
             
        if (!(match instanceof SingleMatch)) {
             throw new IllegalArgumentException("Only single matches can be submitted directly");
        }
        
        SingleMatch singleMatch = (SingleMatch) match;
        
        if (singleMatch.getStatus() != MatchStatus.COMPLETED) {
             throw new IllegalArgumentException("Match is not completed");
        }

        if (leagueMatchRepository.findByLeagueAndMatch(league, singleMatch).isPresent()) {
             throw new IllegalArgumentException("Match already submitted to this league");
        }
        
        LeagueMatch leagueMatch = LeagueMatch.builder()
              .league(league)
              .match(singleMatch)
              .submittedBy(submittor)
              .status(league.isAutoAcceptGames() ? LeagueApprovalStatus.APPROVED : LeagueApprovalStatus.PENDING)
              .build();
              
        leagueMatch = leagueMatchRepository.save(leagueMatch);
        
        if (leagueMatch.getStatus() == LeagueApprovalStatus.APPROVED) {
            processMatchPoints(leagueMatch);
        }
    }
    
    @Transactional
    public void submitTournament(Long leagueId, Long tournamentId, User submittor) {
        League league = leagueRepository.findById(leagueId)
             .orElseThrow(() -> new IllegalArgumentException("League not found"));
             
        Tournament tournament = tournamentRepository.findById(tournamentId)
             .orElseThrow(() -> new IllegalArgumentException("Tournament not found"));
             
        if (tournament.getStatus() != TournamentStatus.COMPLETED) {
             throw new IllegalArgumentException("Tournament is not completed");
        }
        
        if (leagueTournamentRepository.findByLeagueAndTournament(league, tournament).isPresent()) {
             throw new IllegalArgumentException("Tournament already submitted to this league");
        }

        LeagueTournament leagueTournament = LeagueTournament.builder()
              .league(league)
              .tournament(tournament)
              .submittedBy(submittor)
              .status(league.isAutoAcceptTournaments() ? LeagueApprovalStatus.APPROVED : LeagueApprovalStatus.PENDING)
              .build();
              
        leagueTournament = leagueTournamentRepository.save(leagueTournament);
        
        if (leagueTournament.getStatus() == LeagueApprovalStatus.APPROVED) {
            processTournamentPoints(leagueTournament);
        }
    }

    @Transactional
    public void approveMatch(Long leagueMatchId, User approver) {
        LeagueMatch leagueMatch = leagueMatchRepository.findById(leagueMatchId)
             .orElseThrow(() -> new IllegalArgumentException("League Match request not found"));
             
        if (!leagueMatch.getLeague().getOwner().getId().equals(approver.getId())) {
             throw new IllegalArgumentException("Only owner can approve matches");
        }

        if (leagueMatch.getStatus() != LeagueApprovalStatus.PENDING) {
             throw new IllegalArgumentException("Request is not pending");
        }
        
        leagueMatch.setStatus(LeagueApprovalStatus.APPROVED);
        leagueMatch = leagueMatchRepository.save(leagueMatch);
        
        processMatchPoints(leagueMatch);
    }
    
    @Transactional
    public void approveTournament(Long leagueTournamentId, User approver) {
        LeagueTournament leagueTournament = leagueTournamentRepository.findById(leagueTournamentId)
             .orElseThrow(() -> new IllegalArgumentException("League Tournament request not found"));
             
        if (!leagueTournament.getLeague().getOwner().getId().equals(approver.getId())) {
             throw new IllegalArgumentException("Only owner can approve tournaments");
        }

        if (leagueTournament.getStatus() != LeagueApprovalStatus.PENDING) {
             throw new IllegalArgumentException("Request is not pending");
        }
        
        leagueTournament.setStatus(LeagueApprovalStatus.APPROVED);
        leagueTournament = leagueTournamentRepository.save(leagueTournament);
        
        processTournamentPoints(leagueTournament);
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
        
        int pointsToAdd = league.getPointsParticipation();
        
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

    private void processTournamentPoints(LeagueTournament leagueTournament) {
        if (leagueTournament.getProcessedAt() != null) return;
        
        League league = leagueTournament.getLeague();
        Tournament tournament = leagueTournament.getTournament();
        
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
                 
                 int pointsToAdd = league.getPointsParticipation();
                 int rankPoints = (totalParticipants - rank + 1) * league.getPointsPerParticipant();
                 pointsToAdd += rankPoints;
                 
                 member.setPoints(member.getPoints() + pointsToAdd);
                 member.setTournamentsPlayed(member.getTournamentsPlayed() + 1);
                 
                 if (rank == 1) {
                     member.setTournamentWins(member.getTournamentWins() + 1);
                 }
                 
                 leagueMemberRepository.save(member);
             }
        }
        
        leagueTournament.setProcessedAt(LocalDateTime.now());
        leagueTournamentRepository.save(leagueTournament);
    }
}
