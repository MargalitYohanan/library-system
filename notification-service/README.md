# 📧 Notification & Email Microservice

An independent microservice that serves as the central communication channel of the **Community Library Platform**.
It manages, schedules, and sends dynamic email notifications to community members.

---

## 🚀 Prerequisites

- Docker Desktop
- Java 17+
- Maven (or the bundled Maven Wrapper)

---

## 🐘 Step 1: Start the database

This service is part of a larger system with one shared database. **Do not run `docker-compose` from this folder** — run it from the project's root directory (`library-system/`):

```bash
cd ../
docker-compose up -d
```

This starts a shared PostgreSQL instance, which includes `library_notifications_db`.

---

## ☕ Step 2: Start the service

**Option A — simplest:** run the whole system via `docker-compose up -d` from the root (`notification-service` comes up automatically on port `8082`).

**Option B — run standalone from IntelliJ / terminal**, against the DB already running in Docker:

1. Update `application.properties` to match the shared DB (not the old `notification_db`/`myuser`/`mypassword`):
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/library_notifications_db
   spring.datasource.username=library_admin
   spring.datasource.password=UserService123
   ```
2. Run it:
   - In IntelliJ: open `NotificationServiceApplication.java` and click ▶️ Run.
   - In a terminal: `./mvnw spring-boot:run`
3. The service will run locally on port `8080`.

> ⚠️ **Gmail credentials:** `spring.mail.username` and `spring.mail.password` are read from the environment variables `MAIL_USERNAME` and `MAIL_APP_PASSWORD` (a Gmail **App Password**, not your regular password) — never hardcode them in `application.properties`. Set them in a local `.env` file (see `.env.example` at the project root) or as Environment Variables in your IDE's Run Configuration.

---

## ✅ Health check

Once the application is up:
- Via full docker-compose: `http://localhost:8082/api/notification-types/GetAllNotificationType`
- Running locally: `http://localhost:8080/api/notification-types/GetAllNotificationType`

---

## 🛠️ Main Technologies

Java 17 · Spring Boot · Spring Data JPA · PostgreSQL · Docker · Maven · Spring Scheduler · Java Mail Sender
