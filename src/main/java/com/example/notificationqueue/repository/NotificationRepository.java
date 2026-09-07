package com.example.notificationqueue.repository;

import com.example.notificationqueue.model.Notification;
import com.example.notificationqueue.model.NotificationChannel;
import com.example.notificationqueue.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);
    
    Page<Notification> findByChannel(NotificationChannel channel, Pageable pageable);
    
    Page<Notification> findByStatusAndChannel(NotificationStatus status, NotificationChannel channel, Pageable pageable);

    @Query(value = "SELECT * FROM notifications WHERE status = 'PENDING' AND (scheduled_at IS NULL OR scheduled_at <= :currentTime) ORDER BY CASE priority WHEN 'HIGH' THEN 1 WHEN 'NORMAL' THEN 2 WHEN 'LOW' THEN 3 END ASC, created_at ASC", nativeQuery = true)
    List<Notification> findPendingNotificationsToProcess(Instant currentTime);

    @Query("SELECT n.status, COUNT(n) FROM Notification n GROUP BY n.status")
    List<Object[]> countNotificationsByStatus();
}
