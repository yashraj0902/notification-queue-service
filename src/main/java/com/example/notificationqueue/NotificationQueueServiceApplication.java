package com.example.notificationqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationQueueServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationQueueServiceApplication.class, args);
    }
}
