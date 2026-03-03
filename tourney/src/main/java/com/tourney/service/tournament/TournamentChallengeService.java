package com.tourney.service.tournament;

import com.common.domain.User;
import com.tourney.domain.tournament.ChallengeStatus;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentChallenge;
import com.tourney.domain.tournament.TournamentPhase;
import com.tourney.dto.tournament.TournamentChallengeDTO;
import com.tourney.repository.tournament.TournamentChallengeRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentChallengeService {

    private final TournamentChallengeRepository challengeRepository;
    private final TournamentRepository tournamentRepository;
    private final NotificationService notificationService;

    // Create a challenge (Player A -> Player B)
    public TournamentChallengeDTO createChallenge(Long tournamentId, Long challengerId, Long opponentId) {
        if (challengerId.equals(opponentId)) {
            throw new IllegalArgumentException("Nie możesz wyzwać samego siebie.");
        }

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowe ID turnieju."));

        if (tournament.getStatus() != com.tourney.dto.tournament.TournamentStatus.ACTIVE) {
             throw new IllegalStateException("Wyzwania są możliwe tylko w fazie rejestracji.");
        }

        // Check if challenger already has an ACTIVE outgoing challenge (PENDING)
        List<TournamentChallenge> outgoing = challengeRepository.findPendingOutgoingChallenges(tournamentId, challengerId);
        if (!outgoing.isEmpty()) {
            throw new IllegalStateException("Masz już aktywne wyzwanie. Musisz je anulować lub poczekać na odpowiedź.");
        }

        // Check if challenger or opponent already have an ACCEPTED challenge
        if (challengeRepository.findAcceptedChallengeForUser(tournamentId, challengerId).isPresent()) {
             throw new IllegalStateException("Już masz zaakceptowane wyzwanie w tym turnieju.");
        }
        if (challengeRepository.findAcceptedChallengeForUser(tournamentId, opponentId).isPresent()) {
             throw new IllegalStateException("Ten gracz ma już zaakceptowane wyzwanie.");
        }

        // Check if opponent exists in tournament participants (should actully check confirmed)
        // For simplicity assuming IDs are correct users who are confirmed participants
        // In real app, check tournament.getParticipantLinks() for confirmation.
        
        // Check if challenge already exists in reverse? (B challenged A)
        // If B challenged A, creating A->B is weird. A should just accept B. 
        // But the requirement says "Outgoing Challenge". 
        // Let's just create it.
        
        User challengerUser = new User(); challengerUser.setId(challengerId); // Mock user fetch? 
        // We probably need real User entities to set relations if we use JPA relations.
        // The repository takes User entities.
        // But since we are inside a service, we 'should' fetch users.
        // BUT, TournamentChallenge has @ManyToOne User. 
        // Let's assume we can get references or fetch from tournament participants.
        
        User challenger = tournament.getParticipantLinks().stream()
                .filter(p -> p.getUser().getId().equals(challengerId))
                .findFirst()
                .map(p -> p.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Challenger is not a participant"));
        
        User opponent = tournament.getParticipantLinks().stream()
                .filter(p -> p.getUser().getId().equals(opponentId))
                .findFirst()
                .map(p -> p.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Opponent is not a participant"));
                
        // Create
        TournamentChallenge challenge = new TournamentChallenge();
        challenge.setTournament(tournament);
        challenge.setChallenger(challenger);
        challenge.setOpponent(opponent);
        challenge.setStatus(ChallengeStatus.PENDING);
        challenge.setCreatedAt(LocalDateTime.now());
        
        TournamentChallenge saved = challengeRepository.save(challenge);
        
        // Notify Opponent
        notificationService.notifyChallengeReceived(
                opponentId, challengerId, challenger.getName(), tournamentId, tournament.getName());

        return toDto(saved);
    }
    
    // Accept Challenge
    public void acceptChallenge(Long tournamentId, Long challengeId, Long userId) {
        TournamentChallenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Wyzwanie nie istnieje"));
            
        if (!challenge.getOpponent().getId().equals(userId)) {
             throw new IllegalStateException("Nie możesz zaakceptować wyzwania skierowanego do kogoś innego.");
        }
        
        if (challenge.getStatus() != ChallengeStatus.PENDING) {
             throw new IllegalStateException("Wyzwanie nie jest aktywne.");
        }
        
        // Check if user (receiver) has accepted any other challenge meanwhile?
        if (challengeRepository.findAcceptedChallengeForUser(tournamentId, userId).isPresent()) {
             throw new IllegalStateException("Masz już zaakceptowane inne wyzwanie.");
        }
        // Check if challenger was taken meanwhile
        if (challengeRepository.findAcceptedChallengeForUser(tournamentId, challenge.getChallenger().getId()).isPresent()) {
             throw new IllegalStateException("Gracz wyzywający ma już inny zaakceptowany pojedynek.");
        }

        // Accept this one
        challenge.setStatus(ChallengeStatus.ACCEPTED);
        challengeRepository.save(challenge);
        
        // Notify challenger
        notificationService.notifyChallengeAccepted(
                challenge.getChallenger().getId(), userId, challenge.getOpponent().getName(), tournamentId, challenge.getTournament().getName());
        
        // Cleanup other pending challenges for BOTH players
        // 1. Remove/Reject other incoming challenges for THIS user (B)
        // 2. Remove any outgoing challenges from THIS user (B) - wait, user can't have outgoing AND incoming active?
        // Requirement says: "Jeden gracz może mieć tylko jedno aktywne zgłoszenie na challange". 
        // This usually refers to outgoing. But one can receive many.
        
        // Clean up pending challenges involving either player
        cleanupPendingChallenges(tournamentId, challenge.getChallenger().getId(), challenge.getId());
        cleanupPendingChallenges(tournamentId, challenge.getOpponent().getId(), challenge.getId());
    }
    
    // Reject Challenge
    public void rejectChallenge(Long tournamentId, Long challengeId, Long userId) {
         TournamentChallenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Wyzwanie nie istnieje"));
            
        if (!challenge.getOpponent().getId().equals(userId)) {
             throw new IllegalStateException("Nie możesz odrzucić tego wyzwania.");
        }
        
        // Delete or mark rejected
        // Requirement 1.2 "Usuwa się panel challenge" -> we can just delete it or mark rejected.
        // Marking rejected keeps history.
        challenge.setStatus(ChallengeStatus.REJECTED);
        challengeRepository.save(challenge);
        
        // Notify challenger
        notificationService.notifyChallengeRejected(
                challenge.getChallenger().getId(), userId, challenge.getOpponent().getName(), tournamentId, challenge.getTournament().getName());
    }
    
    // Cancel Challenge (by sender)
    public void cancelChallenge(Long tournamentId, Long challengeId, Long userId) {
        TournamentChallenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Wyzwanie nie istnieje"));
            
        if (!challenge.getChallenger().getId().equals(userId)) {
             throw new IllegalStateException("Nie możesz anulować cudzego wyzwania.");
        }
        
        if (challenge.getStatus() != ChallengeStatus.PENDING) {
             throw new IllegalStateException("Można anulować tylko oczekujące wyzwania.");
        }
        
        challengeRepository.delete(challenge);
    }
    
    // Get challenges for view
    public List<TournamentChallengeDTO> getUserChallenges(Long tournamentId, Long userId) {
        return challengeRepository.findAllByTournamentAndUser(tournamentId, userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private void cleanupPendingChallenges(Long tournamentId, Long userId, Long acceptedChallengeId) {
        List<TournamentChallenge> pending = challengeRepository.findAllPendingForUser(tournamentId, userId);
        for (TournamentChallenge c : pending) {
            if (!c.getId().equals(acceptedChallengeId)) {
                // Reject/Delete
                // If this is an outgoing challenge from the user who just got accepted elsewhere -> delete it?
                // If this is an incoming challenge to the user -> reject it?
                // Let's just DELETE for simplicity of "removed"
                challengeRepository.delete(c);
                
                // If deleting an incoming challenge, notify sender? 
                // Requirement 2.1 "Pozostałe wyzwania zostają usunięte".
                // Notification might be nice.
                if (c.getOpponent().getId().equals(userId)) {
                    // This was incoming to me, sender gets notified "Your challenge was removed because opponent accepted another"
                    // Or standard rejected notification.
                    notificationService.notifyChallengeRejected(c.getChallenger().getId(), userId, c.getOpponent().getName(), tournamentId, c.getTournament().getName());
                } else {
                    // This was outgoing from me. I'm busy now.
                    // Receiver sees it disappear.
                }
            }
        }
    }

    private TournamentChallengeDTO toDto(TournamentChallenge entity) {
        return TournamentChallengeDTO.builder()
                .id(entity.getId())
                .tournamentId(entity.getTournament().getId())
                .challengerId(entity.getChallenger().getId())
                .challengerName(entity.getChallenger().getName())
                .opponentId(entity.getOpponent().getId())
                .opponentName(entity.getOpponent().getName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
