package com.tourney.repository.systems;

import com.tourney.domain.systems.PrimaryMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrimaryMissionRepository extends JpaRepository<PrimaryMission, Long> {
    List<PrimaryMission> findByGameSystemIdOrderByNameAsc(Long gameSystemId);
}