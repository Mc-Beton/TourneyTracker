package com.tourney.service.notification;

import com.tourney.domain.games.Match;
import com.tourney.domain.notification.Notification;
import com.tourney.domain.notification.NotificationType;
import com.tourney.domain.tournament.Tournament;
import com.tourney.domain.tournament.TournamentRound;
import com.tourney.dto.notification.NotificationDTO;
import com.tourney.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

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
    @Transactional(readOnly = true)
    public List<NotificationDTO> getRecentNotifications(Long userId, int limit) {
        log.debug("Getting recent notifications for user {}", userId);
        if (userId == null) {
            return List.of();
        }

        int safeLimit = Math.max(1, Math.min(limit, 50));
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId) {
        log.debug("Getting unread count for user {}", userId);
        if (userId == null) {
            return 0;
        }

        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return (int) Math.min(Integer.MAX_VALUE, count);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        log.debug("Marking notification {} as read for user {}", notificationId, userId);
        if (userId == null || notificationId == null) {
            return;
        }

        notificationRepository.findByIdAndUserId(notificationId, userId)
                .ifPresent(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.debug("Marking all notifications as read for user {}", userId);
        if (userId == null) {
            return;
        }

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadFalse(userId);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public void createNotification(Long userId, NotificationType type, Long tournamentId, 
                                  String tournamentName, String message, Long relatedEntityId, 
                                  String relatedEntityName) {
        log.info("Creating notification for user {}: type={}, tournament={}, message={}", 
                userId, type, tournamentName, message);

        if (userId == null || type == null || tournamentId == null || tournamentName == null || message == null) {
            log.warn("Skipping notification creation due to missing required fields: userId={}, type={}, tournamentId={}, tournamentName={}, messagePresent={}",
                    userId, type, tournamentId, tournamentName, message != null);
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTournamentId(tournamentId);
        notification.setTournamentName(tournamentName);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setTriggeredByUserId(relatedEntityId);
        notification.setTriggeredByUserName(relatedEntityName);

        notificationRepository.save(notification);
    }

    private NotificationDTO toDto(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .tournamentId(notification.getTournamentId())
                .tournamentName(notification.getTournamentName())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .triggeredByUserName(notification.getTriggeredByUserName())
                .build();
    }
}
