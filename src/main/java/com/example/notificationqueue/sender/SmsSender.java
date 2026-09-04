package com.example.notificationqueue.sender;

import com.example.notificationqueue.model.Notification;
import com.example.notificationqueue.model.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
public class SmsSender implements NotificationSender {
    
    private final Random random = new Random();

    @Override
    public boolean send(Notification notification) {
        log.info("[SMS] Sending to {}: {}", notification.getRecipient(), notification.getMessage());
        
        // Simulate ~15% failure rate
        if (random.nextInt(100) < 15) {
            log.warn("[SMS] Failed to send to {}", notification.getRecipient());
            return false;
        }
        
        return true;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
}
