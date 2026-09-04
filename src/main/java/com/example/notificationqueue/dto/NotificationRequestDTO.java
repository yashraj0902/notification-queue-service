package com.example.notificationqueue.dto;

import com.example.notificationqueue.model.NotificationChannel;
import com.example.notificationqueue.model.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class NotificationRequestDTO {

    @NotBlank(message = "Recipient is required")
    private String recipient;

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    private NotificationPriority priority;

    private Instant scheduledAt;
}
