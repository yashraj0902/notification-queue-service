# Notification Queue Service

Hey! Welcome to the Notification Queue Service. 

## What is this project?

If you've ever bought something online, you know you don't get the "Order Confirmed" email the *exact* millisecond you click buy. It usually takes a few seconds or a minute. 

This project simulates that real-world behavior. It's a Spring Boot backend API that accepts notification requests (like Email, SMS, or Push) and immediately puts them into a database queue. 

Behind the scenes, a background worker wakes up every 5 seconds. It looks at the database, grabs the pending messages (handling the `HIGH` priority ones first), and simulates sending them out. If a message fails to send (which I've randomly set to happen 15% of the time for realism), the worker leaves it in the queue and tries again on the next run, up to 3 times.

## Tech Stack

Here's what I used to build this:
* **Java 17** 
* **Spring Boot 3** (Spring Web, Spring Data JPA, Spring Scheduling)
* **PostgreSQL** (to store our queue safely)
* **Lombok** (to save time writing boilerplate code like getters/setters)
* **Swagger / OpenAPI** (for testing the API easily in a browser)

## How to run it locally

**Step 1: Set up the Database**
You'll need PostgreSQL running on your machine. Just create a blank database called `notification_db`.
*(Note: The app expects the username as `postgres` and password as `postgres` on port `5432`. You can change this in `src/main/resources/application.properties` if your local setup is different).*

If you use Docker, you can spin one up instantly by running this in your terminal:
```bash
docker run --name postgres-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=notification_db -p 5432:5432 -d postgres
```

**Step 2: Start the app**
Open your terminal in the project folder and run:
```bash
mvn spring-boot:run
```

## How to actually use it

Once the app is running (it usually starts on port 8080), here is how you can play around with it:

### 1. The easy way (Swagger UI)
Open your browser and go to [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). You'll see a nice graphical interface where you can click around and test all the API endpoints without writing any code.

### 2. The Postman way
I've included a file called `NotificationQueueService.postman_collection.json` in the root folder. Just import it into Postman and all the requests are ready to go with example data.

### 3. The Terminal way (cURL)
Want to see the queue in action? Open a new terminal and send this request to create a new notification:

```bash
curl -X POST http://localhost:8080/api/notifications \
-H "Content-Type: application/json" \
-d '{
  "recipient": "test@example.com",
  "channel": "EMAIL",
  "subject": "Hello!",
  "message": "Testing the background worker.",
  "priority": "HIGH"
}'
```

### Watch the Magic Happen
After you send that request, look back at the terminal where your Spring Boot app is running. Within 5 seconds, you'll see the background worker wake up, find your `PENDING` message in the database, and process it. 

Try sending a few requests at once with different priorities (`HIGH`, `NORMAL`, `LOW`) and watch the logs to see how the worker sorts and handles them!
