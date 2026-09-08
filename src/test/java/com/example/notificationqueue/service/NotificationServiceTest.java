package com.example.notificationqueue.service;

import com.example.notificationqueue.dto.NotificationRequestDTO;
import com.example.notificationqueue.dto.NotificationResponseDTO;
import com.example.notificationqueue.exception.IllegalNotificationStateException;
import com.example.notificationqueue.exception.ResourceNotFoundException;
import com.example.notificationqueue.model.Notification;
import com.example.notificationqueue.model.NotificationChannel;
import com.example.notificationqueue.model.NotificationPriority;
import com.example.notificationqueue.model.NotificationStatus;
import com.example.notificationqueue.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // We "mock" the repository so we don't need a real Postgres database running just to run our tests.
        notificationRepository = mock(NotificationRepository.class);
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void testCreateNotification_Success() {
        // 1. Arrange (Set up the test data)
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setRecipient("test@example.com");
        request.setChannel(NotificationChannel.EMAIL);
        request.setMessage("Hello JUnit");
        request.setPriority(NotificationPriority.HIGH);

        Notification savedNotification = new Notification();
        savedNotification.setId(1L);
        savedNotification.setRecipient("test@example.com");
        savedNotification.setChannel(NotificationChannel.EMAIL);
        savedNotification.setMessage("Hello JUnit");
        savedNotification.setStatus(NotificationStatus.PENDING);

        // Tell our fake database to return 'savedNotification' when save() is called
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // 2. Act (Actually call the method we are testing)
        NotificationResponseDTO response = notificationService.createNotification(request);

        // 3. Assert (Check if the results are what we expect)
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getRecipient());
        assertEquals(NotificationStatus.PENDING, response.getStatus());
        
        // Verify that the repository's save method was indeed called exactly once
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testGetNotificationById_NotFound_ThrowsException() {
        // Arrange
        // Simulate the database finding absolutely nothing for ID 99
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        // Expect a ResourceNotFoundException to be thrown when we ask for ID 99
        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.getNotificationById(99L);
        });
    }

    @Test
    void testCancelNotification_AlreadySent_ThrowsException() {
        // Arrange
        Notification sentNotification = new Notification();
        sentNotification.setId(1L);
        sentNotification.setStatus(NotificationStatus.SENT); // Already sent!

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sentNotification));

        // Act & Assert
        assertThrows(IllegalNotificationStateException.class, () -> {
            notificationService.cancelNotification(1L);
        });
        
        // Verify that the delete method was NEVER called because it threw an error first
        verify(notificationRepository, never()).delete(any(Notification.class));
    }
}
