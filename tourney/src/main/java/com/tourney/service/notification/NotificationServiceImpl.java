package com.tourney.service.notification;

import com.tourney.domain.games.Match;
import com.tourney.domain.notification.NotificationType;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.dto.notification.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void notifyRoundStart(Tournament tournament, TournamentRound round) {
        log.info("Round {} started for tournament {}", round.getRoundNumber(), tournament.getName());
        // TODO: Implement actual notification logic
    }

    @Override
    public void notifyRoundEndingSoon(Tournament tournament, TournamentRound round, Duration timeLeft) {
        log.info("Round {} ending soon ({} minutes left) for tournament {}", 
                round.getRoundNumber(), timeLeft.toMinutes(), tournament.getName());
        // TODO: Implement actual notification logic
    }

    @Override
    public void notifyMissingResults(Tournament tournament, Match match) {
        log.info("Missing results for match {} in tournament {}", match.getId(), tournament.getName());
        // TODO: Implement actual notification logic
    }

    @Override
    public List<NotificationDTO> getRecentNotifications(Long userId, int limit) {
        log.debug("Getting recent notifications for user {}", userId);
        // TODO: Implement actual notification retrieval
        return Collections.emptyList();
    }

    @Override
    public int getUnreadCount(Long userId) {
        log.debug("Getting unread count for user {}", userId);
        // TODO: Implement actual unread count
        return 0;
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        log.debug("Marking notification {} as read for user {}", notificationId, userId);
        // TODO: Implement actual mark as read
    }

    @Override
    public void markAllAsRead(Long userId) {
        log.debug("Marking all notifications as read for user {}", userId);
        // TODO: Implement actual mark all as read
    }

    @Override
    public void createNotification(Long userId, NotificationType type, Long tournamentId, 
                                  String tournamentName, String message, Long relatedEntityId, 
                                  String relatedEntityName) {
        log.info("Creating notification for user {}: type={}, tournament={}, message={}", 
                userId, type, tournamentName, message);
        // TODO: Implement actual notification creation
    }
}
