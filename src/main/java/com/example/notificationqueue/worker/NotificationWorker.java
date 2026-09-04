package com.example.notificationqueue.worker;

import com.example.notificationqueue.model.Notification;
import com.example.notificationqueue.model.NotificationStatus;
import com.example.notificationqueue.repository.NotificationRepository;
import com.example.notificationqueue.sender.NotificationSender;
import com.example.notificationqueue.sender.NotificationSenderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorker {

    private final NotificationRepository notificationRepository;
    private final NotificationSenderFactory senderFactory;

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processNotifications() {
        log.info("Starting notification processing job...");
        
        List<Notification> pendingNotifications = notificationRepository
                .findPendingNotificationsToProcess(Instant.now());
                
        if (pendingNotifications.isEmpty()) {
            log.info("No pending notifications to process.");
            return;
        }

        log.info("Found {} pending notifications.", pendingNotifications.size());

        for (Notification notification : pendingNotifications) {
            try {
                processSingleNotification(notification);
            } catch (Exception e) {
                log.error("Error processing notification ID: {}", notification.getId(), e);
            }
        }
        
        log.info("Finished notification processing job.");
    }

    private void processSingleNotification(Notification notification) {
        log.info("Processing notification ID: {} with priority: {}", notification.getId(), notification.getPriority());
        
        NotificationSender sender = senderFactory.getSender(notification.getChannel());
        
        // Increment attempt counter
        notification.setAttempts(notification.getAttempts() + 1);
        
        boolean success = sender.send(notification);
        
        if (success) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            log.info("Notification ID: {} marked as SENT.", notification.getId());
        } else {
            if (notification.getAttempts() >= notification.getMaxAttempts()) {
                notification.setStatus(NotificationStatus.FAILED);
                log.warn("Notification ID: {} max attempts reached. Marked as FAILED.", notification.getId());
            } else {
                log.warn("Notification ID: {} sending failed. Will retry on next run.", notification.getId());
                // Stays PENDING
            }
        }
        
        notificationRepository.save(notification);
    }
}
