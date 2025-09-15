package com.tourney.mapper.games;

import com.tourney.domain.games.Match;
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

        return MatchDTO.builder()
                .id(match.getId())
                .startTime(match.getStartTime())
                .gameDurationMinutes(match.getGameDurationMinutes())
                .resultSubmissionDeadline(match.getResultSubmissionDeadline())
                .roundId(match.getTournamentRound() != null ? match.getTournamentRound().getId() : null)
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

    public Match toEntity(MatchDTO dto) {
        if (dto == null) {
            return null;
        }

        Match match = new Match();
        match.setId(dto.getId());
        match.setStartTime(dto.getStartTime());
        match.setGameDurationMinutes(dto.getGameDurationMinutes());
        match.setResultSubmissionDeadline(dto.getResultSubmissionDeadline());
        match.setMatchResult(dto.getMatchResult());

        if (dto.getRounds() != null) {
            match.setRounds(dto.getRounds().stream()
                    .map(matchRoundMapper::toEntity)
                    .collect(Collectors.toList()));
        }

        return match;
    }
}