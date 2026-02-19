package com.tourney.repository;

import com.tourney.domain.notification.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    long countByUserIdAndReadFalse(Long userId);
    
    List<Notification> findByUserIdAndReadFalse(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
