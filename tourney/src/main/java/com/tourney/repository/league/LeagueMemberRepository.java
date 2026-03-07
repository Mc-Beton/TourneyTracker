package com.tourney.repository.league;

import com.common.domain.User;
import com.tourney.domain.league.League;
import com.tourney.domain.league.LeagueMember;
import com.tourney.domain.league.LeagueMemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueMemberRepository extends JpaRepository<LeagueMember, Long> {
    
    Optional<LeagueMember> findByLeagueAndUser(League league, User user);
    
    List<LeagueMember> findByLeague(League league);
    
    List<LeagueMember> findByLeagueOrderByPointsDesc(League league);
    
    Page<LeagueMember> findByLeagueAndStatus(League league, LeagueMemberStatus status, Pageable pageable);
    
    Page<LeagueMember> findByUserAndStatus(User user, LeagueMemberStatus status, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM LeagueMember m WHERE m.league = :league AND m.user = :user AND m.status = 'APPROVED'")
    boolean isApprovedMember(League league, User user);
}
