package com.example.notificationqueue.dto;

import com.example.notificationqueue.model.NotificationChannel;
import com.example.notificationqueue.model.NotificationPriority;
import com.example.notificationqueue.model.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponseDTO {
    private Long id;
    private String recipient;
    private NotificationChannel channel;
    private String subject;
    private String message;
    private NotificationStatus status;
    private NotificationPriority priority;
    private int attempts;
    private int maxAttempts;
    private Instant createdAt;
    private Instant scheduledAt;
    private Instant sentAt;
}
