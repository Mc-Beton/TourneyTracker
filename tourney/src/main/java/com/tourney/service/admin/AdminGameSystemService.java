package com.tourney.service.admin;

import com.tourney.domain.systems.*;
import com.tourney.dto.systems.admin.*;
import com.tourney.repository.systems.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminGameSystemService {

    private final GameSystemRepository gameSystemRepository;
    private final ArmyFactionRepository armyFactionRepository;
    private final ArmyRepository armyRepository;
    private final DeploymentRepository deploymentRepository;
    private final PrimaryMissionRepository primaryMissionRepository;

    // ==================== GAME SYSTEMS ====================

    public AdminGameSystemDTO createGameSystem(CreateGameSystemDTO dto) {
        GameSystem gameSystem = new GameSystem();
        gameSystem.setName(dto.getName());
        gameSystem.setDefaultRoundNumber(dto.getDefaultRoundNumber());
        gameSystem.setPrimaryScoreEnabled(dto.getPrimaryScoreEnabled() != null ? dto.getPrimaryScoreEnabled() : true);
        gameSystem.setSecondaryScoreEnabled(dto.getSecondaryScoreEnabled() != null ? dto.getSecondaryScoreEnabled() : true);
        gameSystem.setThirdScoreEnabled(dto.getThirdScoreEnabled() != null ? dto.getThirdScoreEnabled() : false);
        gameSystem.setAdditionalScoreEnabled(dto.getAdditionalScoreEnabled() != null ? dto.getAdditionalScoreEnabled() : false);

        gameSystem = gameSystemRepository.save(gameSystem);
        return toAdminDTO(gameSystem);
    }

    public AdminGameSystemDTO updateGameSystem(Long id, UpdateGameSystemDTO dto) {
        GameSystem gameSystem = gameSystemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game system not found"));

        if (dto.getName() != null) {
            gameSystem.setName(dto.getName());
        }
        if (dto.getDefaultRoundNumber() != null) {
            gameSystem.setDefaultRoundNumber(dto.getDefaultRoundNumber());
        }
        if (dto.getPrimaryScoreEnabled() != null) {
            gameSystem.setPrimaryScoreEnabled(dto.getPrimaryScoreEnabled());
        }
        if (dto.getSecondaryScoreEnabled() != null) {
            gameSystem.setSecondaryScoreEnabled(dto.getSecondaryScoreEnabled());
        }
        if (dto.getThirdScoreEnabled() != null) {
            gameSystem.setThirdScoreEnabled(dto.getThirdScoreEnabled());
        }
        if (dto.getAdditionalScoreEnabled() != null) {
            gameSystem.setAdditionalScoreEnabled(dto.getAdditionalScoreEnabled());
        }

        gameSystem = gameSystemRepository.save(gameSystem);
        return toAdminDTO(gameSystem);
    }

    @Transactional(readOnly = true)
    public List<AdminGameSystemDTO> getAllGameSystems() {
        return gameSystemRepository.findAllByOrderByNameAsc().stream()
                .map(this::toAdminDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminGameSystemDTO getGameSystemById(Long id) {
        GameSystem gameSystem = gameSystemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game system not found"));
        return toAdminDTO(gameSystem);
    }

    public void deleteGameSystem(Long id) {
        if (!gameSystemRepository.existsById(id)) {
            throw new RuntimeException("Game system not found");
        }
        gameSystemRepository.deleteById(id);
    }

    // ==================== ARMY FACTIONS ====================

    public ArmyFaction createArmyFaction(CreateArmyFactionDTO dto) {
        GameSystem gameSystem = gameSystemRepository.findById(dto.getGameSystemId())
                .orElseThrow(() -> new RuntimeException("Game system not found"));

        ArmyFaction faction = new ArmyFaction();
        faction.setName(dto.getName());
        faction.setGameSystem(gameSystem);

        return armyFactionRepository.save(faction);
    }

    public ArmyFaction updateArmyFaction(Long id, String name) {
        ArmyFaction faction = armyFactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Army faction not found"));
        faction.setName(name);
        return armyFactionRepository.save(faction);
    }

    public void deleteArmyFaction(Long id) {
        if (!armyFactionRepository.existsById(id)) {
            throw new RuntimeException("Army faction not found");
        }
        armyFactionRepository.deleteById(id);
    }

    // ==================== ARMIES ====================

    public Army createArmy(CreateArmyDTO dto) {
        ArmyFaction faction = armyFactionRepository.findById(dto.getArmyFactionId())
                .orElseThrow(() -> new RuntimeException("Army faction not found"));

        Army army = new Army();
        army.setName(dto.getName());
        army.setArmyFaction(faction);

        return armyRepository.save(army);
    }

    public Army updateArmy(Long id, String name) {
        Army army = armyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Army not found"));
        army.setName(name);
        return armyRepository.save(army);
    }

    public void deleteArmy(Long id) {
        if (!armyRepository.existsById(id)) {
            throw new RuntimeException("Army not found");
        }
        armyRepository.deleteById(id);
    }

    // ==================== DEPLOYMENTS ====================

    public Deployment createDeployment(CreateDeploymentDTO dto) {
        GameSystem gameSystem = gameSystemRepository.findById(dto.getGameSystemId())
                .orElseThrow(() -> new RuntimeException("Game system not found"));

        Deployment deployment = new Deployment();
        deployment.setName(dto.getName());
        deployment.setGameSystem(gameSystem);

        return deploymentRepository.save(deployment);
    }

    public Deployment updateDeployment(Long id, String name) {
        Deployment deployment = deploymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deployment not found"));
        deployment.setName(name);
        return deploymentRepository.save(deployment);
    }

    public void deleteDeployment(Long id) {
        if (!deploymentRepository.existsById(id)) {
            throw new RuntimeException("Deployment not found");
        }
        deploymentRepository.deleteById(id);
    }

    // ==================== PRIMARY MISSIONS ====================

    public PrimaryMission createPrimaryMission(CreatePrimaryMissionDTO dto) {
        GameSystem gameSystem = gameSystemRepository.findById(dto.getGameSystemId())
                .orElseThrow(() -> new RuntimeException("Game system not found"));

        PrimaryMission mission = new PrimaryMission();
        mission.setName(dto.getName());
        mission.setGameSystem(gameSystem);

        return primaryMissionRepository.save(mission);
    }

    public PrimaryMission updatePrimaryMission(Long id, String name) {
        PrimaryMission mission = primaryMissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Primary mission not found"));
        mission.setName(name);
        return primaryMissionRepository.save(mission);
    }

    public void deletePrimaryMission(Long id) {
        if (!primaryMissionRepository.existsById(id)) {
            throw new RuntimeException("Primary mission not found");
        }
        primaryMissionRepository.deleteById(id);
    }

    // ==================== HELPERS ====================

    private AdminGameSystemDTO toAdminDTO(GameSystem gameSystem) {
        return AdminGameSystemDTO.builder()
                .id(gameSystem.getId())
                .name(gameSystem.getName())
                .defaultRoundNumber(gameSystem.getDefaultRoundNumber())
                .primaryScoreEnabled(gameSystem.isPrimaryScoreEnabled())
                .secondaryScoreEnabled(gameSystem.isSecondaryScoreEnabled())
                .thirdScoreEnabled(gameSystem.isThirdScoreEnabled())
                .additionalScoreEnabled(gameSystem.isAdditionalScoreEnabled())
                .build();
    }
}
