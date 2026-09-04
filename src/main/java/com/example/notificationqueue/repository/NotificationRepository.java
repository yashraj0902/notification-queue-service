package com.example.notificationqueue.repository;

import com.example.notificationqueue.model.Notification;
import com.example.notificationqueue.model.NotificationChannel;
import com.example.notificationqueue.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByStatus(NotificationStatus status);
    
    List<Notification> findByChannel(NotificationChannel channel);
    
    List<Notification> findByStatusAndChannel(NotificationStatus status, NotificationChannel channel);

    @Query(value = "SELECT * FROM notifications WHERE status = 'PENDING' AND (scheduled_at IS NULL OR scheduled_at <= :currentTime) ORDER BY CASE priority WHEN 'HIGH' THEN 1 WHEN 'NORMAL' THEN 2 WHEN 'LOW' THEN 3 END ASC, created_at ASC", nativeQuery = true)
    List<Notification> findPendingNotificationsToProcess(Instant currentTime);

    @Query("SELECT n.status, COUNT(n) FROM Notification n GROUP BY n.status")
    List<Object[]> countNotificationsByStatus();
}
