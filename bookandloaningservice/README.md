# 📖 Book & Loaning Management System

Manages the book catalog and the end-to-end loan lifecycle for the **Community Library Platform** microservices system.

## 📋 Prerequisites

- Docker Desktop
- Java 21
- IntelliJ IDEA (or any other Java IDE)

## 🚀 Running the Service

This service is part of a larger system (User Service, Notification Service, AI Service, and one shared database). **Do not run `docker-compose` from this folder** — always run it from the project's root directory (`library-system/`).

### Step 1: Start the databases

From the project **root** (not from here):

```bash
cd ../
docker-compose up -d
```

This starts a single shared PostgreSQL instance, which includes this service's database: `library_books_db`.

### Step 2: Start the service

**Option A — simplest: run the whole system via docker-compose** (already covered in Step 1 — `book-service` comes up automatically on port `8081`, no extra steps needed).

**Option B — run only this service from IntelliJ** (against the DB already running in Docker):

1. Open `application.properties` and make sure the connection details match the shared database at the project root:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/library_books_db
   spring.datasource.username=library_admin
   spring.datasource.password=UserService123
   ```
2. Run the main class `BookAndLoaningManagementSystemApplication`.
3. The application will run locally on port `8080`.

## 🌐 Access

- Via full docker-compose: `http://localhost:8081/api/books`
- Running locally from IntelliJ: `http://localhost:8080/api/books`

## 🛠️ Main Technologies

Java 21 · Spring Boot · Spring Data JPA · PostgreSQL · Docker · Maven
