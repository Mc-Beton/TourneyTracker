package com.tourney.domain.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private Long tournamentId;

    @Column(nullable = false)
    private String tournamentName;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // For some notification types, we might want to store who triggered it
    private Long triggeredByUserId;
    private String triggeredByUserName;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Constructors
    public Notification() {
    }

    public Notification(Long userId, NotificationType type, Long tournamentId, String tournamentName, String message) {
        this.userId = userId;
        this.type = type;
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;
        this.message = message;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Long getTriggeredByUserId() {
        return triggeredByUserId;
    }

    public void setTriggeredByUserId(Long triggeredByUserId) {
        this.triggeredByUserId = triggeredByUserId;
    }

    public String getTriggeredByUserName() {
        return triggeredByUserName;
    }

    public void setTriggeredByUserName(String triggeredByUserName) {
        this.triggeredByUserName = triggeredByUserName;
    }
}
