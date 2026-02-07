package com.tourney.mapper.games;

import com.tourney.domain.games.Match;
import com.tourney.domain.games.TournamentMatch;
import com.tourney.dto.games.MatchDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MatchMapper {

    private final MatchRoundMapper matchRoundMapper;

    public MatchDTO toDto(Match match) {
        if (match == null) {
            return null;
        }

        Long roundId = null;
        if (match instanceof TournamentMatch tournamentMatch) {
            roundId = tournamentMatch.getTournamentRound() != null ? 
                      tournamentMatch.getTournamentRound().getId() : null;
        }

        return MatchDTO.builder()
                .id(match.getId())
                .startTime(match.getStartTime())
                .gameDurationMinutes(match.getGameDurationMinutes())
                .resultSubmissionDeadline(match.getResultSubmissionDeadline())
                .roundId(roundId)
                .player1Id(match.getPlayer1() != null ? match.getPlayer1().getId() : null)
                .player2Id(match.getPlayer2() != null ? match.getPlayer2().getId() : null)
                .rounds(match.getRounds() != null ?
                        match.getRounds().stream()
                                .map(matchRoundMapper::toDto)
                                .collect(Collectors.toList()) :
                        null)
                .matchResult(match.getMatchResult())
                .build();
    }

    // toEntity removed - use specific SingleMatchService/TournamentMatchService instead
}