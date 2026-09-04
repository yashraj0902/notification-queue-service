package com.example.notificationqueue.sender;

import com.example.notificationqueue.model.Notification;
import com.example.notificationqueue.model.NotificationChannel;

public interface NotificationSender {
    boolean send(Notification notification);
    NotificationChannel getChannel();
}
