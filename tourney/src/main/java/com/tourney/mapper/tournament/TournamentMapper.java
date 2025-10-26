package com.tourney.mapper.tournament;

import com.tourney.domain.games.Match;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.user.User;
import com.tourney.dto.tournament.ActiveTournamentDTO;
import com.tourney.dto.tournament.TournamentResponseDTO;
import com.tourney.service.player.PlayerMatchService;
import com.tourney.util.ActionRequiredChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TournamentMapper {

    private final PlayerMatchService playerMatchService;

    public TournamentResponseDTO toDto(Tournament tournament) {
        if (tournament == null) {
            return null;
        }

        return TournamentResponseDTO.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .startDate(tournament.getStartDate())
                .numberOfRounds(tournament.getNumberOfRounds())
                .roundDurationMinutes(tournament.getRoundDurationMinutes())
                .gameSystemId(tournament.getGameSystem().getId())
                .organizerId(tournament.getOrganizer().getId())
                .participantIds(tournament.getParticipants().stream()
                        .map(User::getId)
                        .collect(Collectors.toList()))
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

}

