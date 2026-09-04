package com.example.notificationqueue.controller;

import com.example.notificationqueue.dto.NotificationRequestDTO;
import com.example.notificationqueue.dto.NotificationResponseDTO;
import com.example.notificationqueue.model.NotificationChannel;
import com.example.notificationqueue.model.NotificationStatus;
import com.example.notificationqueue.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create a new notification")
    public ResponseEntity<NotificationResponseDTO> createNotification(@Valid @RequestBody NotificationRequestDTO requestDTO) {
        NotificationResponseDTO response = notificationService.createNotification(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all notifications (supports optional filtering)")
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationChannel channel) {
        List<NotificationResponseDTO> responses = notificationService.getNotifications(status, channel);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single notification by its ID")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(@PathVariable Long id) {
        NotificationResponseDTO response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get count of notifications grouped by their status")
    public ResponseEntity<Map<String, Long>> getNotificationStats() {
        Map<String, Long> stats = notificationService.getNotificationStats();
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a PENDING notification")
    public ResponseEntity<Void> cancelNotification(@PathVariable Long id) {
        notificationService.cancelNotification(id);
        return ResponseEntity.noContent().build();
    }
}
