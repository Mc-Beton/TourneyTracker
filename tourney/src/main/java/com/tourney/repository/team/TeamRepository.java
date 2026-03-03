package com.tourney.repository.team;

import com.common.domain.User;
import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);
    List<Team> findByOwner(User owner);
    List<Team> findByGameSystem(GameSystem gameSystem);
}
