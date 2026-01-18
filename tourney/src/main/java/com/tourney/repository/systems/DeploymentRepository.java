package com.tourney.repository.systems;

import com.tourney.domain.systems.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeploymentRepository extends JpaRepository<Deployment, Long> {
    List<Deployment> findByGameSystemIdOrderByNameAsc(Long gameSystemId);
}