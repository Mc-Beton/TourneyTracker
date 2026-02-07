package com.tourney.service.tournament;

import com.tourney.domain.notification.NotificationType;
import com.tourney.domain.participant.ArmyListStatus;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.systems.Army;
import com.tourney.domain.systems.ArmyFaction;
import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.participant.ArmyListDetailsDTO;
import com.tourney.dto.participant.ReviewArmyListDTO;
import com.tourney.dto.participant.SubmitArmyListDTO;
import com.tourney.dto.participant.TournamentParticipantDTO;
import com.tourney.repository.participant.TournamentParticipantRepository;
import com.tourney.repository.systems.ArmyFactionRepository;
import com.tourney.repository.systems.ArmyRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.service.notification.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TournamentParticipantService {

    private final TournamentParticipantRepository participantRepository;
    private final TournamentRepository tournamentRepository;
    private final ArmyFactionRepository armyFactionRepository;
    private final ArmyRepository armyRepository;
    private final NotificationService notificationService;

    @Transactional
    public TournamentParticipantDTO submitArmyList(Long tournamentId, Long userId, SubmitArmyListDTO dto) {
        TournamentParticipant participant = participantRepository
                .findByTournamentIdAndUserId(tournamentId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        // Pobierz faction i army
        ArmyFaction faction = armyFactionRepository.findById(dto.getArmyFactionId())
                .orElseThrow(() -> new EntityNotFoundException("Army faction not found"));

        Army army = armyRepository.findById(dto.getArmyId())
                .orElseThrow(() -> new EntityNotFoundException("Army not found"));

        // Zaktualizuj dane rozpiski
        participant.setArmyFaction(faction);
        participant.setArmy(army);
        participant.setArmyListContent(dto.getArmyListContent());
        participant.setArmyListSubmitted(true);
        participant.setArmyListStatus(ArmyListStatus.PENDING);
        participant.setArmyListSubmittedAt(LocalDateTime.now());

        participantRepository.save(participant);

        // Notify organizer
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found"));
        
        notificationService.createNotification(
                tournament.getOrganizer().getId(),
                NotificationType.ARMY_LIST_SUBMITTED,
                tournamentId,
                tournament.getName(),
                participant.getUser().getName() + " przesłał rozpiskę",
                userId,
                participant.getUser().getName()
        );

        return mapToDTO(participant);
    }

    @Transactional(readOnly = true)
    public ArmyListDetailsDTO getArmyListDetails(Long tournamentId, Long userId) {
        TournamentParticipant participant = participantRepository
                .findByTournamentIdAndUserId(tournamentId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        return mapToArmyListDetailsDTO(participant);
    }

    @Transactional(readOnly = true)
    public ArmyListDetailsDTO getArmyListDetailsAsOrganizer(Long tournamentId, Long userId, Long organizerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found"));

        if (!tournament.getOrganizer().getId().equals(organizerId)) {
            throw new SecurityException("Only tournament organizer can view participant army lists");
        }

        TournamentParticipant participant = participantRepository
                .findByTournamentIdAndUserId(tournamentId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        return mapToArmyListDetailsDTO(participant);
    }

    @Transactional
    public TournamentParticipantDTO reviewArmyList(Long tournamentId, Long userId, ReviewArmyListDTO dto, Long organizerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found"));

        if (!tournament.getOrganizer().getId().equals(organizerId)) {
            throw new SecurityException("Only tournament organizer can review army lists");
        }

        TournamentParticipant participant = participantRepository
                .findByTournamentIdAndUserId(tournamentId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        if (dto.isApproved()) {
            participant.setArmyListStatus(ArmyListStatus.APPROVED);
            participant.setRejectionReason(null);
        } else {
            participant.setArmyListStatus(ArmyListStatus.REJECTED);
            participant.setRejectionReason(dto.getRejectionReason());
        }

        participant.setArmyListReviewedAt(LocalDateTime.now());
        participantRepository.save(participant);

        // Notify participant
        notificationService.createNotification(
                userId,
                dto.isApproved() ? NotificationType.ARMY_LIST_APPROVED : NotificationType.ARMY_LIST_REJECTED,
                tournamentId,
                tournament.getName(),
                dto.isApproved() 
                    ? "Twoja rozpiska została zatwierdzona"
                    : "Twoja rozpiska została odrzucona: " + dto.getRejectionReason(),
                organizerId,
                tournament.getOrganizer().getName()
        );

        return mapToDTO(participant);
    }

    @Transactional
    public TournamentParticipantDTO setPaymentStatus(Long tournamentId, Long userId, boolean isPaid, Long organizerId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new EntityNotFoundException("Tournament not found"));

        if (!tournament.getOrganizer().getId().equals(organizerId)) {
            throw new SecurityException("Only tournament organizer can change payment status");
        }

        TournamentParticipant participant = participantRepository
                .findByTournamentIdAndUserId(tournamentId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found"));

        participant.setPaid(isPaid);
        participantRepository.save(participant);

        // Notify participant when payment is confirmed
        if (isPaid) {
            notificationService.createNotification(
                    userId,
                    NotificationType.PAYMENT_CONFIRMED,
                    tournamentId,
                    tournament.getName(),
                    "Twoja płatność została potwierdzona",
                    organizerId,
                    tournament.getOrganizer().getName()
            );
        }

        return mapToDTO(participant);
    }

    private TournamentParticipantDTO mapToDTO(TournamentParticipant p) {
        return TournamentParticipantDTO.builder()
                .userId(p.getUserId())
                .name(p.getUser() != null ? p.getUser().getName() : null)
                .email(p.getUser() != null ? p.getUser().getEmail() : null)
                .confirmed(p.isConfirmed())
                .isPaid(p.isPaid())
                .armyListStatus(p.getArmyListStatus())
                .armyFactionName(p.getArmyFaction() != null ? p.getArmyFaction().getName() : null)
                .armyName(p.getArmy() != null ? p.getArmy().getName() : null)
                .build();
    }

    private ArmyListDetailsDTO mapToArmyListDetailsDTO(TournamentParticipant p) {
        return ArmyListDetailsDTO.builder()
                .userId(p.getUserId())
                .userName(p.getUser() != null ? p.getUser().getName() : null)
                .armyFactionId(p.getArmyFaction() != null ? p.getArmyFaction().getId() : null)
                .armyFactionName(p.getArmyFaction() != null ? p.getArmyFaction().getName() : null)
                .armyId(p.getArmy() != null ? p.getArmy().getId() : null)
                .armyName(p.getArmy() != null ? p.getArmy().getName() : null)
                .armyListContent(p.getArmyListContent())
                .status(p.getArmyListStatus())
                .submittedAt(p.getArmyListSubmittedAt())
                .reviewedAt(p.getArmyListReviewedAt())
                .rejectionReason(p.getRejectionReason())
                .build();
    }
}
