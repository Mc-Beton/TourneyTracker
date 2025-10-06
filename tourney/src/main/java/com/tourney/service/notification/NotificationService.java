package com.tourney.service.notification;

import com.tourney.domain.games.Match;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRound;

import java.time.Duration;

public interface NotificationService {
    void notifyRoundStart(Tournament tournament, TournamentRound round);
    void notifyRoundEndingSoon(Tournament tournament, TournamentRound round, Duration timeLeft);
    void notifyMissingResults(Tournament tournament, Match match);
}

