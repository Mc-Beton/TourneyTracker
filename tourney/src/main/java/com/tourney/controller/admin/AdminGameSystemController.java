package com.tourney.controller.admin;

import com.tourney.domain.systems.*;
import com.tourney.dto.systems.admin.*;
import com.tourney.dto.systems.IdNameDTO;
import com.tourney.repository.systems.*;
import com.tourney.service.admin.AdminGameSystemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing game systems, factions, armies, deployments, and missions.
 * TODO: Add @PreAuthorize("hasRole('ADMIN')") annotations once security is fully configured
 */
@RestController
@RequestMapping("/api/admin/game-systems")
@RequiredArgsConstructor
public class AdminGameSystemController {

    private final AdminGameSystemService adminGameSystemService;
    private final ArmyFactionRepository armyFactionRepository;
    private final ArmyRepository armyRepository;
    private final DeploymentRepository deploymentRepository;
    private final PrimaryMissionRepository primaryMissionRepository;

    // ==================== GAME SYSTEMS ====================

    @GetMapping
    public ResponseEntity<List<AdminGameSystemDTO>> getAllGameSystems() {
        return ResponseEntity.ok(adminGameSystemService.getAllGameSystems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminGameSystemDTO> getGameSystem(@PathVariable Long id) {
        return ResponseEntity.ok(adminGameSystemService.getGameSystemById(id));
    }

    @PostMapping
    public ResponseEntity<AdminGameSystemDTO> createGameSystem(@Valid @RequestBody CreateGameSystemDTO dto) {
        AdminGameSystemDTO created = adminGameSystemService.createGameSystem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminGameSystemDTO> updateGameSystem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGameSystemDTO dto) {
        AdminGameSystemDTO updated = adminGameSystemService.updateGameSystem(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGameSystem(@PathVariable Long id) {
        adminGameSystemService.deleteGameSystem(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ARMY FACTIONS ====================

    @GetMapping("/{gameSystemId}/army-factions")
    public ResponseEntity<List<IdNameDTO>> getArmyFactions(@PathVariable Long gameSystemId) {
        List<IdNameDTO> factions = armyFactionRepository.findByGameSystemIdOrderByNameAsc(gameSystemId).stream()
                .map(f -> IdNameDTO.builder().id(f.getId()).name(f.getName()).build())
                .toList();
        return ResponseEntity.ok(factions);
    }

    @PostMapping("/army-factions")
    public ResponseEntity<ArmyFaction> createArmyFaction(@Valid @RequestBody CreateArmyFactionDTO dto) {
        ArmyFaction created = adminGameSystemService.createArmyFaction(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/army-factions/{id}")
    public ResponseEntity<ArmyFaction> updateArmyFaction(
            @PathVariable Long id,
            @RequestParam String name) {
        ArmyFaction updated = adminGameSystemService.updateArmyFaction(id, name);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/army-factions/{id}")
    public ResponseEntity<Void> deleteArmyFaction(@PathVariable Long id) {
        adminGameSystemService.deleteArmyFaction(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ARMIES ====================

    @GetMapping("/army-factions/{factionId}/armies")
    public ResponseEntity<List<IdNameDTO>> getArmies(@PathVariable Long factionId) {
        List<IdNameDTO> armies = armyRepository.findByArmyFactionIdOrderByNameAsc(factionId).stream()
                .map(a -> IdNameDTO.builder().id(a.getId()).name(a.getName()).build())
                .toList();
        return ResponseEntity.ok(armies);
    }

    @PostMapping("/armies")
    public ResponseEntity<Army> createArmy(@Valid @RequestBody CreateArmyDTO dto) {
        Army created = adminGameSystemService.createArmy(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/armies/{id}")
    public ResponseEntity<Army> updateArmy(
            @PathVariable Long id,
            @RequestParam String name) {
        Army updated = adminGameSystemService.updateArmy(id, name);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/armies/{id}")
    public ResponseEntity<Void> deleteArmy(@PathVariable Long id) {
        adminGameSystemService.deleteArmy(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== DEPLOYMENTS ====================

    @GetMapping("/{gameSystemId}/deployments")
    public ResponseEntity<List<IdNameDTO>> getDeployments(@PathVariable Long gameSystemId) {
        List<IdNameDTO> deployments = deploymentRepository.findByGameSystemIdOrderByNameAsc(gameSystemId).stream()
                .map(d -> IdNameDTO.builder().id(d.getId()).name(d.getName()).build())
                .toList();
        return ResponseEntity.ok(deployments);
    }

    @PostMapping("/deployments")
    public ResponseEntity<Deployment> createDeployment(@Valid @RequestBody CreateDeploymentDTO dto) {
        Deployment created = adminGameSystemService.createDeployment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/deployments/{id}")
    public ResponseEntity<Deployment> updateDeployment(
            @PathVariable Long id,
            @RequestParam String name) {
        Deployment updated = adminGameSystemService.updateDeployment(id, name);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/deployments/{id}")
    public ResponseEntity<Void> deleteDeployment(@PathVariable Long id) {
        adminGameSystemService.deleteDeployment(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PRIMARY MISSIONS ====================

    @GetMapping("/{gameSystemId}/primary-missions")
    public ResponseEntity<List<IdNameDTO>> getPrimaryMissions(@PathVariable Long gameSystemId) {
        List<IdNameDTO> missions = primaryMissionRepository.findByGameSystemIdOrderByNameAsc(gameSystemId).stream()
                .map(m -> IdNameDTO.builder().id(m.getId()).name(m.getName()).build())
                .toList();
        return ResponseEntity.ok(missions);
    }

    @PostMapping("/primary-missions")
    public ResponseEntity<PrimaryMission> createPrimaryMission(@Valid @RequestBody CreatePrimaryMissionDTO dto) {
        PrimaryMission created = adminGameSystemService.createPrimaryMission(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/primary-missions/{id}")
    public ResponseEntity<PrimaryMission> updatePrimaryMission(
            @PathVariable Long id,
            @RequestParam String name) {
        PrimaryMission updated = adminGameSystemService.updatePrimaryMission(id, name);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/primary-missions/{id}")
    public ResponseEntity<Void> deletePrimaryMission(@PathVariable Long id) {
        adminGameSystemService.deletePrimaryMission(id);
        return ResponseEntity.noContent().build();
    }
}
