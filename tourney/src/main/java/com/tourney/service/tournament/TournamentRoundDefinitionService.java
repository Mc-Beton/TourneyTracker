package com.tourney.service.tournament;

import com.tourney.domain.systems.Deployment;
import com.tourney.domain.systems.PrimaryMission;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRoundDefinition;
import com.tourney.dto.tournament.TournamentRoundDefinitionDTO;
import com.tourney.dto.tournament.UpdateRoundDefinitionDTO;
import com.tourney.repository.systems.DeploymentRepository;
import com.tourney.repository.systems.PrimaryMissionRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.repository.TournamentRoundDefinitionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentRoundDefinitionService {
    
    private final TournamentRoundDefinitionRepository roundDefinitionRepository;
    private final TournamentRepository tournamentRepository;
    private final DeploymentRepository deploymentRepository;
    private final PrimaryMissionRepository primaryMissionRepository;
    
    public List<TournamentRoundDefinitionDTO> getRoundDefinitions(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found"));
        
        return roundDefinitionRepository.findByTournamentIdOrderByRoundNumberAsc(tournamentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public TournamentRoundDefinitionDTO updateRoundDefinition(
            Long tournamentId,
            Integer roundNumber,
            UpdateRoundDefinitionDTO dto,
            Long currentUserId) {
        
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found"));
        
        // Check if user is organizer
        if (!tournament.getOrganizer().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only organizer can edit round definitions");
        }
        
        TournamentRoundDefinition definition = roundDefinitionRepository
                .findByTournamentIdOrderByRoundNumberAsc(tournamentId)
                .stream()
                .filter(def -> def.getRoundNumber().equals(roundNumber))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Round definition not found"));
        
        // Update deployment
        if (dto.getDeploymentId() != null) {
            Deployment deployment = deploymentRepository.findById(dto.getDeploymentId())
                    .orElseThrow(() -> new EntityNotFoundException("Deployment not found"));
            definition.setDeployment(deployment);
        } else {
            definition.setDeployment(null);
        }
        
        // Update primary mission
        if (dto.getPrimaryMissionId() != null) {
            PrimaryMission mission = primaryMissionRepository.findById(dto.getPrimaryMissionId())
                    .orElseThrow(() -> new EntityNotFoundException("Primary mission not found"));
            definition.setPrimaryMission(mission);
        } else {
            definition.setPrimaryMission(null);
        }
        
        // Update other fields
        if (dto.getIsSplitMapLayout() != null) {
            definition.setIsSplitMapLayout(dto.getIsSplitMapLayout());
        }
        if (dto.getMapLayoutEven() != null) {
            definition.setMapLayoutEven(dto.getMapLayoutEven());
        }
        if (dto.getMapLayoutOdd() != null) {
            definition.setMapLayoutOdd(dto.getMapLayoutOdd());
        }
        if (dto.getByeLargePoints() != null) {
            definition.setByeLargePoints(dto.getByeLargePoints());
        }
        if (dto.getByeSmallPoints() != null) {
            definition.setByeSmallPoints(dto.getByeSmallPoints());
        }
        if (dto.getSplitLargePoints() != null) {
            definition.setSplitLargePoints(dto.getSplitLargePoints());
        }
        if (dto.getSplitSmallPoints() != null) {
            definition.setSplitSmallPoints(dto.getSplitSmallPoints());
        }
        
        TournamentRoundDefinition saved = roundDefinitionRepository.save(definition);
        return toDTO(saved);
    }
    
    private TournamentRoundDefinitionDTO toDTO(TournamentRoundDefinition entity) {
        return TournamentRoundDefinitionDTO.builder()
                .id(entity.getId())
                .roundNumber(entity.getRoundNumber())
                .deploymentId(entity.getDeployment() != null ? entity.getDeployment().getId() : null)
                .deploymentName(entity.getDeployment() != null ? entity.getDeployment().getName() : null)
                .primaryMissionId(entity.getPrimaryMission() != null ? entity.getPrimaryMission().getId() : null)
                .primaryMissionName(entity.getPrimaryMission() != null ? entity.getPrimaryMission().getName() : null)
                .isSplitMapLayout(entity.getIsSplitMapLayout())
                .mapLayoutEven(entity.getMapLayoutEven())
                .mapLayoutOdd(entity.getMapLayoutOdd())
                .byeLargePoints(entity.getByeLargePoints())
                .byeSmallPoints(entity.getByeSmallPoints())
                .splitLargePoints(entity.getSplitLargePoints())
                .splitSmallPoints(entity.getSplitSmallPoints())
                .build();
    }
}
