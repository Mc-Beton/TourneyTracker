package com.tourney.service.notification;

import com.tourney.domain.games.Match;
import com.tourney.domain.notification.NotificationType;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.dto.notification.NotificationDTO;

import java.time.Duration;
import java.util.List;

public interface NotificationService {
    void notifyRoundStart(Tournament tournament, TournamentRound round);
    void notifyRoundEndingSoon(Tournament tournament, TournamentRound round, Duration timeLeft);
    void notifyMissingResults(Tournament tournament, Match match);

    // Challenge notifications
    void notifyChallengeReceived(Long userId, Long challengerId, String challengerName, Long tournamentId, String tournamentName);
    void notifyChallengeAccepted(Long userId, Long opponentId, String opponentName, Long tournamentId, String tournamentName);
    void notifyChallengeRejected(Long userId, Long opponentId, String opponentName, Long tournamentId, String tournamentName);
    
    // League Challenge notifications
    void notifyLeagueChallengeReceived(Long userId, Long challengerId, String challengerName, Long leagueId, String leagueName);
    
    // Additional methods used by controllers/services
    List<NotificationDTO> getRecentNotifications(Long userId, int limit);
    int getUnreadCount(Long userId);
    void markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
    void createNotification(Long userId, NotificationType type, Long tournamentId, String tournamentName, String message, Long relatedEntityId, String relatedEntityName);
}

