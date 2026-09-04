# Notification Queue Service

## Project Overview
The Notification Queue Service is a Spring Boot backend application that decouples the act of requesting a notification from the actual sending process. It uses a persisted database queue and a scheduled worker to process, send, and optionally retry notifications (Email, SMS, Push). 

This design mirrors real-world systems used in e-commerce (e.g., order updates) and appointment reminders where high reliability and asynchronous processing are essential.

## Architecture

```text
+-------------------+       +-----------------------+       +-------------------+
|                   |       |                       |       |                   |
|   Client App      +------>+  Notification API     +------>+  PostgreSQL DB    |
| (Creates Request) | POST  |  (Spring Web MVC)     | Save  | (notifications)   |
|                   |       |                       |       |                   |
+-------------------+       +-----------+-----------+       +---------+---------+
                                        |                             ^
                                        |                             |
                                        v                             | Fetch/Update
                            +-----------------------+                 |
                            |                       |                 |
                            | Scheduled Worker      +-----------------+
                            | (@Scheduled Poller)   |
                            |                       |
                            +-----------+-----------+
                                        |
                                        v
                            +-----------------------+
                            |                       |
                            |  Sender Strategy      |
                            |  (Email/SMS/Push)     |
                            |                       |
                            +-----------------------+
```

## Setup Instructions

### Prerequisites
- **Java 17+**
- **Maven**
- **PostgreSQL** running locally

### PostgreSQL Setup
If you are using Docker, you can quickly start a local Postgres database:
```bash
docker run --name postgres-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=notification_db -p 5432:5432 -d postgres
```
Alternatively, create a database named `notification_db` manually in your local PostgreSQL server.

The application configuration connects using:
- **URL**: `jdbc:postgresql://localhost:5432/notification_db`
- **Username**: `postgres`
- **Password**: `postgres`

### Running the Application
Run the service using Maven:
```bash
mvn spring-boot:run
```

Once started, the API runs on `http://localhost:8080`.

### API Documentation
Swagger UI is available at:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Example API Requests (cURL)

**1. Create a Notification**
```bash
curl -X POST http://localhost:8080/api/notifications \
-H "Content-Type: application/json" \
-d '{
  "recipient": "user@example.com",
  "channel": "EMAIL",
  "subject": "Order Shipped",
  "message": "Your order #1234 has been shipped.",
  "priority": "HIGH"
}'
```

**2. List All Notifications**
```bash
curl -X GET http://localhost:8080/api/notifications
```

**3. Get Filtered Notifications**
```bash
curl -X GET "http://localhost:8080/api/notifications?status=PENDING&channel=EMAIL"
```

**4. Get Notification By ID**
```bash
curl -X GET http://localhost:8080/api/notifications/1
```

**5. Get Notification Stats**
```bash
curl -X GET http://localhost:8080/api/notifications/stats
```

**6. Cancel a Notification**
```bash
curl -X DELETE http://localhost:8080/api/notifications/1
```

## Design Decisions

- **Polling Scheduler (`@Scheduled`)**: Rather than directly calling the sending logic when the API request arrives, the notification is saved to a PostgreSQL database with a `PENDING` status. A background worker periodically polls for these `PENDING` notifications. This decouples the sender from the API, allows requests to return quickly, provides fault tolerance (retries), and enables future-proofing like deferred sending (`scheduledAt`).
- **Status-based State Machine**: Tracking `PENDING`, `SENT`, and `FAILED` makes system behavior observable and easy to query. This provides idempotency—we ensure `SENT` items are never resent—and facilitates the retry logic where items remain `PENDING` until `attempts >= maxAttempts`.
- **Strategy Pattern for Senders**: Instead of complex if-else logic within the worker, the `NotificationSenderFactory` provides the right `NotificationSender` implementation (Email, SMS, Push) dynamically at runtime based on the notification's `channel`. This ensures the code adheres to the Open/Closed Principle (adding new channels only requires a new implementation class, not modifying existing logic).
- **Global Exception Handling**: Uses `@ControllerAdvice` to intercept exceptions (like missing entries or validation errors) and map them to appropriate HTTP statuses (`404 Not Found`, `409 Conflict`, `400 Bad Request`), keeping controllers clean.
