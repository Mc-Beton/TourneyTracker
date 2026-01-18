package com.tourney.controller.systems;

import com.tourney.dto.systems.IdNameDTO;
import com.tourney.repository.systems.DeploymentRepository;
import com.tourney.repository.systems.GameSystemRepository;
import com.tourney.repository.systems.PrimaryMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/systems")
@RequiredArgsConstructor
public class SystemDictionaryController {

    private final GameSystemRepository gameSystemRepository;
    private final DeploymentRepository deploymentRepository;
    private final PrimaryMissionRepository primaryMissionRepository;

    @GetMapping("/game-systems")
    public ResponseEntity<List<IdNameDTO>> getGameSystems() {
        List<IdNameDTO> result = gameSystemRepository.findAllByOrderByNameAsc().stream()
                .map(gs -> IdNameDTO.builder().id(gs.getId()).name(gs.getName()).build())
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{gameSystemId}/deployments")
    public ResponseEntity<List<IdNameDTO>> getDeployments(@PathVariable Long gameSystemId) {
        List<IdNameDTO> result = deploymentRepository.findByGameSystemIdOrderByNameAsc(gameSystemId).stream()
                .map(d -> IdNameDTO.builder().id(d.getId()).name(d.getName()).build())
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{gameSystemId}/primary-missions")
    public ResponseEntity<List<IdNameDTO>> getPrimaryMissions(@PathVariable Long gameSystemId) {
        List<IdNameDTO> result = primaryMissionRepository.findByGameSystemIdOrderByNameAsc(gameSystemId).stream()
                .map(pm -> IdNameDTO.builder().id(pm.getId()).name(pm.getName()).build())
                .toList();
        return ResponseEntity.ok(result);
    }
}