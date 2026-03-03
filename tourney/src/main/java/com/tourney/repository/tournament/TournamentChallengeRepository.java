package com.tourney.repository.tournament;

import com.tourney.domain.tournament.TournamentChallenge;
import com.tourney.domain.tournament.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentChallengeRepository extends JpaRepository<TournamentChallenge, Long> {
    
    // Find challenges for a user in a tournament (incoming or outgoing)
    @Query("SELECT c FROM TournamentChallenge c WHERE c.tournament.id = :tournamentId AND (c.challenger.id = :userId OR c.opponent.id = :userId)")
    List<TournamentChallenge> findAllByTournamentAndUser(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);

    // Find outgoing pending challenges for a user (to enforce limit of 1)
    @Query("SELECT c FROM TournamentChallenge c WHERE c.tournament.id = :tournamentId AND c.challenger.id = :userId AND c.status = 'PENDING'")
    List<TournamentChallenge> findPendingOutgoingChallenges(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);

    // Check if a user is part of any accepted challenge
    @Query("SELECT c FROM TournamentChallenge c WHERE c.tournament.id = :tournamentId AND c.status = 'ACCEPTED' AND (c.challenger.id = :userId OR c.opponent.id = :userId)")
    Optional<TournamentChallenge> findAcceptedChallengeForUser(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);

    // Find all accepted challenges for a tournament (for pairing)
    List<TournamentChallenge> findAllByTournamentIdAndStatus(Long tournamentId, ChallengeStatus status);

    // Find all pending challenges for a specific user to iterate through and reject if needed
    @Query("SELECT c FROM TournamentChallenge c WHERE c.tournament.id = :tournamentId AND c.status = 'PENDING' AND (c.challenger.id = :userId OR c.opponent.id = :userId)")
    List<TournamentChallenge> findAllPendingForUser(@Param("tournamentId") Long tournamentId, @Param("userId") Long userId);
}
