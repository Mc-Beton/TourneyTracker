package com.tourney.repository.league;

import com.tourney.domain.league.LeagueChallenge;
import com.tourney.domain.league.LeagueApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueChallengeRepository extends JpaRepository<LeagueChallenge, Long> {

    List<LeagueChallenge> findByChallengedIdAndStatus(Long challengedId, LeagueApprovalStatus status);
    List<LeagueChallenge> findByChallengerIdAndStatus(Long challengerId, LeagueApprovalStatus status);
}
