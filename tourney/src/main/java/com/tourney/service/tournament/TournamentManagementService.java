package com.tourney.service.tournament;

import com.tourney.domain.systems.GameSystem;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.domain.tournament.TournamentScoring;
import com.tourney.domain.user.User;
import com.tourney.dto.tournament.CreateTournamentDTO;
import com.tourney.dto.tournament.TournamentStatus;
import com.tourney.dto.tournament.UpdateTournamentDTO;
import com.tourney.repository.systems.GameSystemRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentManagementService {
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final GameSystemRepository gameSystemRepository;

    public Tournament createTournament(CreateTournamentDTO dto, Long organizerId) {
        validateTournamentData(dto);

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono organizatora o ID: " + organizerId));

        GameSystem gameSystem = gameSystemRepository.findById(dto.getGameSystemId())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono systemu gry o ID: " + dto.getGameSystemId()));

        Tournament tournament = new Tournament();
        tournament.setName(dto.getName());
        tournament.setDescription(dto.getDescription());
        tournament.setStartDate(dto.getStartDate());
        tournament.setEndDate(dto.getEndDate());
        tournament.setNumberOfRounds(dto.getNumberOfRounds());
        tournament.setRoundDurationMinutes(dto.getRoundDurationMinutes());
        tournament.setGameSystem(gameSystem);
        tournament.setOrganizer(organizer);
        tournament.setType(dto.getType());
        tournament.setMaxParticipants(dto.getMaxParticipants());
        tournament.setRegistrationDeadline(dto.getRegistrationDeadline());
        tournament.setLocation(dto.getLocation());
        tournament.setVenue(dto.getVenue());
        tournament.setStatus(TournamentStatus.DRAFT);
        tournament.setRounds(createInitialRounds(tournament));

        // Tworzenie i konfiguracja systemu punktacji
        TournamentScoring scoring = new TournamentScoring();
        scoring.setTournament(tournament);
        scoring.setScoringSystem(dto.getScoringSystem());
        scoring.setEnabledScoreTypes(dto.getEnabledScoreTypes());
        scoring.setRequireAllScoreTypes(dto.isRequireAllScoreTypes());
        scoring.setMinScore(dto.getMinScore());
        scoring.setMaxScore(dto.getMaxScore());
        tournament.setTournamentScoring(scoring);

        return tournamentRepository.save(tournament);
    }



    public Tournament updateTournament(Long tournamentId, UpdateTournamentDTO dto) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));

        validateTournamentUpdate(tournament, dto);

        tournament.setName(dto.getName());
        tournament.setStartDate(dto.getStartDate());
        tournament.setRoundDurationMinutes(dto.getRoundDurationMinutes());

        return tournamentRepository.save(tournament);
    }

    @Transactional
    public void deleteTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));

        validateTournamentDeletion(tournament);
        tournamentRepository.delete(tournament);
    }

    @Transactional
    public Tournament addParticipant(Long tournamentId, Long userId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika o ID: " + userId));

        if (tournament.getParticipants().contains(user)) {
            throw new RuntimeException("Użytkownik jest już uczestnikiem turnieju");
        }

        tournament.getParticipants().add(user);
        return tournamentRepository.save(tournament);
    }

    @Transactional
    public Tournament removeParticipant(Long tournamentId, Long userId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika o ID: " + userId));

        if (!tournament.getParticipants().contains(user)) {
            throw new RuntimeException("Użytkownik nie jest uczestnikiem turnieju");
        }

        tournament.getParticipants().remove(user);
        return tournamentRepository.save(tournament);
    }

    private List<TournamentRound> createInitialRounds(Tournament tournament) {
        List<TournamentRound> rounds = new ArrayList<>();
        IntStream.rangeClosed(1, tournament.getNumberOfRounds()).forEach(roundNumber -> {
            TournamentRound round = new TournamentRound();
            round.setRoundNumber(roundNumber);
            round.setTournament(tournament);
            rounds.add(round);
        });
        return rounds;
    }

    private void validateTournamentData(CreateTournamentDTO dto) {
        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Data rozpoczęcia turnieju nie może być w przeszłości");
        }
        if (dto.getNumberOfRounds() < 1) {
            throw new RuntimeException("Liczba rund musi być większa niż 0");
        }
        if (dto.getRoundDurationMinutes() < 90) {
            throw new RuntimeException("Czas trwania rundy musi być co najmniej 90 minut");
        }
    }

    private void validateTournamentUpdate(Tournament tournament, UpdateTournamentDTO dto) {
        if (tournament.getRounds().stream().anyMatch(round -> !round.getMatches().isEmpty())) {
            throw new RuntimeException("Nie można modyfikować turnieju, który ma już rozegrane mecze");
        }
        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Data rozpoczęcia turnieju nie może być w przeszłości");
        }
        if (dto.getRoundDurationMinutes() < 90) {
            throw new RuntimeException("Czas trwania rundy musi być co najmniej 90 minut");
        }
    }

    private void validateTournamentDeletion(Tournament tournament) {
        if (tournament.getRounds().stream().anyMatch(round -> !round.getMatches().isEmpty())) {
            throw new RuntimeException("Nie można usunąć turnieju, który ma już rozegrane mecze");
        }
    }
}