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
You'll need a PostgreSQL database to store the queue. 

If you have Docker installed, this is super easy. Just open your terminal in the project folder and run:
```bash
docker-compose up -d
```
This instantly starts a ready-to-go database with the correct name and passwords. 

*(If you aren't using Docker, just manually create a local Postgres database called `notification_db` with the username and password set to `postgres` on port `5432`).*

**Step 2: Start the app**
Open your terminal in the project folder and run:
```bash
mvn spring-boot:run
```

## How to actually use it

Once the app is running (it usually starts on port 8080), here is how you can play around with it:

### 1. The easy way (Swagger UI)
Open your browser and go to [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). You'll see a nice graphical interface where you can click around, create new notifications, and test all the API endpoints without writing any code.

### 2. The Postman way
I've included a file called `NotificationQueueService.postman_collection.json` in the root folder. Just import it into Postman and all the requests are ready to go with example data.
