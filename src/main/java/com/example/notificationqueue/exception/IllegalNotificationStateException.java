package com.example.notificationqueue.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IllegalNotificationStateException extends RuntimeException {
    public IllegalNotificationStateException(String message) {
        super(message);
    }
}
