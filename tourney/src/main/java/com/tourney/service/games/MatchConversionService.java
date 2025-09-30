package com.tourney.service.games;

import com.tourney.domain.games.Match;
import com.tourney.domain.user.User;
import com.tourney.dto.rounds.MatchInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchConversionService {

    public MatchInfo toMatchInfo(Match match) {
        return MatchInfo.builder()
                .matchId(match.getId())
                .tableNumber(match.getTableNumber())
                .player1Name(getPlayerName(match.getPlayer1()))
                .player2Name(getPlayerName(match.getPlayer2()))
                .startTime(match.getStartTime())
                .durationMinutes(match.getGameDurationMinutes())
                .build();
    }

    private String getPlayerName(User player) {
        return player != null ? player.getName() : "Bye";
    }
}