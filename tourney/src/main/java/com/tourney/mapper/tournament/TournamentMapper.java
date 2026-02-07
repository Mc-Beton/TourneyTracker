package com.tourney.mapper.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.participant.TournamentParticipant;
import com.tourney.domain.tournament.Tournament;
import com.tourney.dto.tournament.ActiveTournamentDTO;
import com.tourney.dto.tournament.TournamentResponseDTO;
import com.tourney.service.player.PlayerMatchService;
import com.tourney.util.ActionRequiredChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TournamentMapper {

    private final PlayerMatchService playerMatchService;

    public TournamentResponseDTO toDto(Tournament tournament) {
        if (tournament == null) {
            return null;
        }

        int confirmedCount = (int) tournament.getParticipantLinks().stream()
                .filter(TournamentParticipant::isConfirmed)
                .count();

        return TournamentResponseDTO.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .startDate(tournament.getStartDate())
                .numberOfRounds(tournament.getNumberOfRounds())
                .roundDurationMinutes(tournament.getRoundDurationMinutes())
                .gameSystemId(tournament.getGameSystem() != null ? tournament.getGameSystem().getId() : null)
                .gameSystemName(tournament.getGameSystem() != null ? tournament.getGameSystem().getName() : null)
                .organizerId(tournament.getOrganizer().getId())
                .organizerName(tournament.getOrganizer() != null ? tournament.getOrganizer().getName() : null)
                .participantIds(tournament.getParticipantLinks().stream()
                        .map(TournamentParticipant::getUserId)
                        .collect(Collectors.toList()))
                .location(tournament.getLocation())
                .description(tournament.getDescription())
                .maxParticipants(tournament.getMaxParticipants())
                .armyPointsLimit(tournament.getArmyPointsLimit())
                .confirmedParticipantsCount(confirmedCount)
                .status(tournament.getStatus())
                .scoringSystem(tournament.getTournamentScoring().getScoringSystem())
                .enabledScoreTypes(tournament.getTournamentScoring().getEnabledScoreTypes())
                .requireAllScoreTypes(tournament.getTournamentScoring().isRequireAllScoreTypes())
                .minScore(tournament.getTournamentScoring().getMinScore())
                .maxScore(tournament.getTournamentScoring().getMaxScore())
                .build();
    }

    public ActiveTournamentDTO toActiveDTO(Tournament tournament, Long playerId) {
        Match currentMatch = playerMatchService.getCurrentMatchForPlayer(tournament, playerId);

        return ActiveTournamentDTO.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .currentRound(tournament.getCurrentRound())
                .roundStartTime(tournament.getCurrentRoundStartTime())
                .roundEndTime(tournament.getCurrentRoundEndTime())
                .currentMatchStatus(currentMatch != null ? currentMatch.getStatus() : null)
                .opponent(currentMatch != null ?
                        playerMatchService.getOpponentName(currentMatch, playerId) : null)
                .requiresAction(ActionRequiredChecker.isActionRequired(tournament, playerId))
                .build();
    }

    public List<TournamentResponseDTO> getDtloList(List<Tournament> tournamentList) {
        return tournamentList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}