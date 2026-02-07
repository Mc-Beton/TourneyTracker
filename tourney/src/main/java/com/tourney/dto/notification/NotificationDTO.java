package com.tourney.dto.notification;

import com.tourney.domain.notification.NotificationType;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private Long tournamentId;
    private String tournamentName;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private String triggeredByUserName;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NotificationDTO dto = new NotificationDTO();

        public Builder id(Long id) {
            dto.id = id;
            return this;
        }

        public Builder type(NotificationType type) {
            dto.type = type;
            return this;
        }

        public Builder tournamentId(Long tournamentId) {
            dto.tournamentId = tournamentId;
            return this;
        }

        public Builder tournamentName(String tournamentName) {
            dto.tournamentName = tournamentName;
            return this;
        }

        public Builder message(String message) {
            dto.message = message;
            return this;
        }

        public Builder read(boolean read) {
            dto.read = read;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            dto.createdAt = createdAt;
            return this;
        }

        public Builder triggeredByUserName(String triggeredByUserName) {
            dto.triggeredByUserName = triggeredByUserName;
            return this;
        }

        public NotificationDTO build() {
            return dto;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTriggeredByUserName() {
        return triggeredByUserName;
    }

    public void setTriggeredByUserName(String triggeredByUserName) {
        this.triggeredByUserName = triggeredByUserName;
    }
}
