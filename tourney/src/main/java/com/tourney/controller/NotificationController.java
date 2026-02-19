package com.tourney.controller;

import com.common.security.UserPrincipal;
import com.tourney.dto.notification.NotificationDTO;
import com.tourney.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<NotificationDTO>> getRecentNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<NotificationDTO> notifications = notificationService.getRecentNotifications(currentUser.getId(), limit);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserPrincipal currentUser) {
        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/{id}/mark-read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        notificationService.markAsRead(currentUser.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
