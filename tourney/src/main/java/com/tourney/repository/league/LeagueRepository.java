package com.tourney.repository.league;

import com.tourney.domain.league.League;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {
    Optional<League> findByName(String name);

    @Query("SELECT l FROM League l JOIN LeagueMember lm ON l.id = lm.league.id WHERE lm.user.id = :userId")
    Page<League> findJoinedLeagues(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT l FROM League l WHERE NOT EXISTS (SELECT 1 FROM LeagueMember lm WHERE lm.league.id = l.id AND lm.user.id = :userId)")
    Page<League> findAvailableLeagues(@Param("userId") Long userId, Pageable pageable);
}
