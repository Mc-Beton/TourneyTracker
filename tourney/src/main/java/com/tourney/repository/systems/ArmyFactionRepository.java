package com.tourney.repository.systems;

import com.tourney.domain.systems.ArmyFaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArmyFactionRepository extends JpaRepository<ArmyFaction, Long> {
    List<ArmyFaction> findByGameSystemIdOrderByNameAsc(Long gameSystemId);
}
