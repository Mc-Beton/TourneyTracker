package com.tourney.service.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.notification.NotificationType;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.scores.Score;
import com.tourney.domain.tournament.Tournament;
import com.common.domain.User;
import com.tourney.dto.complex.UserTournamentMatchesDTO;
import com.tourney.dto.matches.MatchDetailsDTO;
import com.tourney.dto.participant.TournamentParticipantDTO;
import com.tourney.dto.scores.RoundScoreDTO;
import com.tourney.repository.games.MatchRepository;
import com.tourney.repository.participant.TournamentParticipantRepository;
import com.tourney.repository.scores.ScoreRepository;
import com.tourney.repository.tournament.TournamentRepository;
import com.tourney.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TournamentUserService {
    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final ScoreRepository scoreRepository;
    private final MatchRepository matchRepository;
    private final NotificationService notificationService;

    public List<UserTournamentMatchesDTO> getUsersMatchesWithScores(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));

        List<Match> tournamentMatches = tournament.getRounds().stream()
                .flatMap(round -> round.getMatches().stream())
                .collect(Collectors.toList());

        // Pobierz wszystkie wyniki dla meczy w turnieju
        List<Score> allScores = scoreRepository.findAllByTournamentId(tournamentId);

        // Grupuj wyniki według użytkownika i rundy meczu
        Map<Long, Map<Long, Long>> roundScores = allScores.stream()
                .collect(Collectors.groupingBy(
                        score -> score.getUser().getId(),
                        Collectors.groupingBy(
                                score -> score.getMatchRound().getId(),
                                Collectors.summingLong(Score::getScore)
                        )
                ));

        return tournament.getParticipantLinks().stream()
                .map(TournamentParticipant::getUser)
                .map(user -> createUserMatchesDTO(user, tournamentMatches, roundScores))
                .collect(Collectors.toList());

    }

    private UserTournamentMatchesDTO createUserMatchesDTO(
            User user,
            List<Match> tournamentMatches,
            Map<Long, Map<Long, Long>> roundScores
    ) {
        List<MatchDetailsDTO> matchDetails = tournamentMatches.stream()
                .filter(match -> match.getPlayer1().getId().equals(user.getId())
                        || match.getPlayer2().getId().equals(user.getId()))
                .map(match -> createMatchDetailsDTO(match, user, roundScores.getOrDefault(user.getId(), Map.of())))
                .collect(Collectors.toList());

        return UserTournamentMatchesDTO.builder()
                .userId(user.getId())
                .userName(user.getName())
                .matches(matchDetails)
                .build();
    }

    private MatchDetailsDTO createMatchDetailsDTO(
            Match match,
            User user,
            Map<Long, Long> userRoundScores
    ) {
        User opponent = match.getPlayer1().getId().equals(user.getId())
                ? match.getPlayer2()
                : match.getPlayer1();

        List<RoundScoreDTO> roundScores = match.getRounds().stream()
                .map(round -> RoundScoreDTO.builder()
                        .roundNumber(round.getRoundNumber())
                        .build())
                .collect(Collectors.toList());

        return MatchDetailsDTO.builder()
                .matchId(match.getId())
                .startTime(match.getStartTime())
                .opponentId(opponent.getId())
                .opponentName(opponent.getName())
                .roundScores(roundScores)
                .build();
    }


    public List<User> getUsersByTournamentId(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .map(t -> t.getParticipantLinks().stream()
                        .map(TournamentParticipant::getUser)
                        .toList())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));
    }


    public List<TournamentParticipantDTO> getParticipants(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));

        return tournament.getParticipantLinks().stream()
                .map(link -> TournamentParticipantDTO.builder()
                        .userId(link.getUser().getId())
                        .name(link.getUser().getName())
                        .email(link.getUser().getEmail())
                        .confirmed(link.isConfirmed())
                        .isPaid(link.isPaid())
                        .armyListStatus(link.getArmyListStatus())
                        .armyFactionName(link.getArmyFaction() != null ? link.getArmyFaction().getName() : null)
                        .armyName(link.getArmy() != null ? link.getArmy().getName() : null)
                        .build())
                .toList();
    }

    public List<TournamentParticipantDTO> getParticipantsByConfirmation(Long tournamentId, boolean confirmed) {
        // Use repository query instead of lazy-loaded relationship to avoid cache issues
        List<TournamentParticipant> participants = participantRepository.findByTournamentIdAndConfirmed(tournamentId, confirmed);
        
        return participants.stream()
                .map(link -> TournamentParticipantDTO.builder()
                        .userId(link.getUser().getId())
                        .name(link.getUser().getName())
                        .email(link.getUser().getEmail())
                        .confirmed(link.isConfirmed())
                        .isPaid(link.isPaid())
                        .armyListStatus(link.getArmyListStatus())
                        .armyFactionName(link.getArmyFaction() != null ? link.getArmyFaction().getName() : null)
                        .armyName(link.getArmy() != null ? link.getArmy().getName() : null)
                        .build())
                .toList();
    }

    @Transactional
    public Tournament setParticipantConfirmation(Long tournamentId, Long userId, boolean confirmed, Long currentUserId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono turnieju o ID: " + tournamentId));

        if (tournament.getOrganizer() == null || tournament.getOrganizer().getId() == null
                || !tournament.getOrganizer().getId().equals(currentUserId)) {
            throw new RuntimeException("Brak uprawnień: tylko organizator może potwierdzać uczestników.");
        }

        TournamentParticipant link = tournament.getParticipantLinks().stream()
                .filter(pl -> pl.getUser() != null && pl.getUser().getId() != null && pl.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Użytkownik o ID: " + userId + " nie jest zapisany do tego turnieju."));

        link.setConfirmed(confirmed);

        Tournament savedTournament = tournamentRepository.save(tournament);

        // Notify participant when confirmed
        if (confirmed) {
            notificationService.createNotification(
                    userId,
                    NotificationType.PARTICIPATION_CONFIRMED,
                    tournament.getId(),
                    tournament.getName(),
                    "Twoje uczestnictwo w turnieju zostało potwierdzone",
                    currentUserId,
                    tournament.getOrganizer().getName()
            );
        }

        return savedTournament;
    }
}