package com.example.notificationqueue.service;

import com.example.notificationqueue.dto.NotificationRequestDTO;
import com.example.notificationqueue.dto.NotificationResponseDTO;
import com.example.notificationqueue.exception.IllegalNotificationStateException;
import com.example.notificationqueue.exception.ResourceNotFoundException;
import com.example.notificationqueue.model.Notification;
import com.example.notificationqueue.model.NotificationChannel;
import com.example.notificationqueue.model.NotificationStatus;
import com.example.notificationqueue.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponseDTO createNotification(NotificationRequestDTO requestDTO) {
        log.info("Creating new notification for recipient: {}", requestDTO.getRecipient());
        
        Notification notification = Notification.builder()
                .recipient(requestDTO.getRecipient())
                .channel(requestDTO.getChannel())
                .subject(requestDTO.getSubject())
                .message(requestDTO.getMessage())
                .priority(requestDTO.getPriority())
                .scheduledAt(requestDTO.getScheduledAt())
                .build();
                
        Notification saved = notificationRepository.save(notification);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getNotifications(NotificationStatus status, NotificationChannel channel, int page, int size) {
        // Create a Pageable object, sorting by newest first
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notificationPage;
        
        if (status != null && channel != null) {
            notificationPage = notificationRepository.findByStatusAndChannel(status, channel, pageable);
        } else if (status != null) {
            notificationPage = notificationRepository.findByStatus(status, pageable);
        } else if (channel != null) {
            notificationPage = notificationRepository.findByChannel(channel, pageable);
        } else {
            notificationPage = notificationRepository.findAll(pageable);
        }
        
        // Convert the Page of Entities into a Page of DTOs
        return notificationPage.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public NotificationResponseDTO getNotificationById(Long id) {
        Notification notification = findNotificationById(id);
        return mapToDTO(notification);
    }

    @Transactional
    public void cancelNotification(Long id) {
        Notification notification = findNotificationById(id);
        
        if (notification.getStatus() == NotificationStatus.SENT) {
            throw new IllegalNotificationStateException("Cannot cancel a notification that has already been SENT");
        }
        
        log.info("Cancelling notification ID: {}", id);
        notificationRepository.delete(notification);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getNotificationStats() {
        List<Object[]> stats = notificationRepository.countNotificationsByStatus();
        return stats.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));
    }

    private Notification findNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
    }

    private NotificationResponseDTO mapToDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .recipient(notification.getRecipient())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .priority(notification.getPriority())
                .attempts(notification.getAttempts())
                .maxAttempts(notification.getMaxAttempts())
                .createdAt(notification.getCreatedAt())
                .scheduledAt(notification.getScheduledAt())
                .sentAt(notification.getSentAt())
                .build();
    }
}
