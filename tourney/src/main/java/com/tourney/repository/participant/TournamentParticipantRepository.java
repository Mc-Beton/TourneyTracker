package com.tourney.repository.participant;

import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.participant.TournamentParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, TournamentParticipantId> {
    Optional<TournamentParticipant> findByTournamentIdAndUserId(Long tournamentId, Long userId);
    List<TournamentParticipant> findByTournamentIdAndConfirmed(Long tournamentId, boolean confirmed);
}
