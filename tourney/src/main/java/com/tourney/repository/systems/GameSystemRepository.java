package com.tourney.repository.systems;

import com.tourney.domain.systems.GameSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameSystemRepository extends JpaRepository<GameSystem, Long> {

    List<GameSystem> findAllByOrderByNameAsc();

}