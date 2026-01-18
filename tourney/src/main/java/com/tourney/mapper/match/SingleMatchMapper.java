package com.tourney.mapper.match;

import com.tourney.domain.games.Match;
import com.tourney.dto.matches.SingleMatchResponseDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SingleMatchMapper {

    public SingleMatchResponseDTO toDto(Match match) {
        if (match == null) {
            return null;
        }

        String player2NameFromUser = match.getPlayer2() != null ? match.getPlayer2().getName() : null;
        String player2NameFromGuest = match.getDetails() != null ? match.getDetails().getGuestPlayer2Name() : null;

        String resolvedPlayer2Name = StringUtils.hasText(player2NameFromUser)
                ? player2NameFromUser
                : (StringUtils.hasText(player2NameFromGuest) ? player2NameFromGuest : null);

        Long gameSystemId = (match.getDetails() != null && match.getDetails().getGameSystem() != null)
                ? match.getDetails().getGameSystem().getId()
                : null;

        String gameSystemName = (match.getDetails() != null && match.getDetails().getGameSystem() != null)
                ? match.getDetails().getGameSystem().getName()
                : null;

        Integer p1Ready = match.isPlayer1Ready() ? 1 : 0;
        Integer p2Ready = match.isPlayer2Ready() ? 1 : 0;

        return SingleMatchResponseDTO.builder()
                .matchId(match.getId())
                .matchName(match.getDetails() != null ? match.getDetails().getMatchName() : null)
                .startTime(match.getStartTime())
                .endTime(match.getGameEndTime())
                .gameSystemId(gameSystemId)
                .gameSystemName(gameSystemName)
                .player1Id(match.getPlayer1() != null ? match.getPlayer1().getId() : null)
                .player1Name(match.getPlayer1() != null ? match.getPlayer1().getName() : null)
                .player1ready(p1Ready)
                .player2Id(match.getPlayer2() != null ? match.getPlayer2().getId() : null)
                .player2ready(p2Ready)
                .player2Name(resolvedPlayer2Name)
                .hotSeat(match.getPlayer2() == null)
                .mode(match.getDetails() != null ? match.getDetails().getMode() : null)
                .build();
    }
}