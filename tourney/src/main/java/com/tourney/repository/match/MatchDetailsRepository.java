package com.tourney.repository.match;

import com.tourney.domain.games.MatchDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchDetailsRepository extends JpaRepository<MatchDetails, Long> {
}