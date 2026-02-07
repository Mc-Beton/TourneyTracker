package com.tourney.repository.systems;

import com.tourney.domain.systems.Army;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArmyRepository extends JpaRepository<Army, Long> {
    List<Army> findByArmyFactionIdOrderByNameAsc(Long armyFactionId);
}
