package com.tourney.repository.tournament;

import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.tournament.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    @Query("""
           SELECT DISTINCT t
           FROM Tournament t
           JOIN t.participantLinks pl
           WHERE pl.user.id = :playerId
             AND t.status IN (com.tourney.dto.tournament.TournamentStatus.ACTIVE,
                              com.tourney.dto.tournament.TournamentStatus.IN_PROGRESS)
           """)

    List<Tournament> findActiveForPlayer(@Param("playerId") Long playerId);

    List<Tournament> findByStatusIn(Collection<TournamentStatus> statuses);

    List<Tournament> findByOrganizerId(Long organizerId);
}