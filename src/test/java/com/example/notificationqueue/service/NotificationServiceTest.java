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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Ensures the test database wipes itself clean after every single test
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        // No Mockito needed! We wipe the real (in-memory) database clean before each test.
        notificationRepository.deleteAll();
    }

    @Test
    void testCreateNotification_Success() {
        // 1. Arrange (Set up the test data)
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setRecipient("purejunit@example.com");
        request.setChannel(NotificationChannel.EMAIL);
        request.setMessage("Hello pure JUnit");
        request.setPriority(NotificationPriority.HIGH);

        // 2. Act (Actually call the method we are testing)
        NotificationResponseDTO response = notificationService.createNotification(request);

        // 3. Assert (Check if the results are what we expect)
        assertNotNull(response);
        assertNotNull(response.getId()); // The real database generated a real ID!
        assertEquals("purejunit@example.com", response.getRecipient());
        assertEquals(NotificationStatus.PENDING, response.getStatus());
        
        // Verify it was actually saved in our in-memory database
        assertEquals(1, notificationRepository.count());
    }

    @Test
    void testGetNotificationById_NotFound_ThrowsException() {
        // Act & Assert
        // Expect a ResourceNotFoundException because the database is totally empty
        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.getNotificationById(999L);
        });
    }

    @Test
    void testCancelNotification_AlreadySent_ThrowsException() {
        // Arrange
        // Actually save a SENT notification into the database
        Notification sentNotification = new Notification();
        sentNotification.setRecipient("test@example.com");
        sentNotification.setChannel(NotificationChannel.SMS);
        sentNotification.setMessage("Testing");
        sentNotification.setStatus(NotificationStatus.SENT); // Already sent!
        sentNotification.setAttempts(1);
        sentNotification.setMaxAttempts(3);
        sentNotification.setPriority(NotificationPriority.NORMAL);
        
        sentNotification = notificationRepository.save(sentNotification);
        
        final Long savedId = sentNotification.getId();

        // Act & Assert
        // Trying to cancel a SENT notification should fail
        assertThrows(IllegalNotificationStateException.class, () -> {
            notificationService.cancelNotification(savedId);
        });
        
        // Verify that it wasn't deleted from the database
        assertTrue(notificationRepository.existsById(savedId));
    }
}
