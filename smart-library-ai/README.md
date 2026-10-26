# 🤖 Smart Library AI

A microservice that provides personalized book recommendations for the **Community Library Platform**, powered by the Groq API.

---

## 📋 Prerequisites

- Docker Desktop
- Java 21
- A Groq API key (see the security note below)

---

## 🚀 Step 1: Start the database

This service is part of a larger system with one shared database. **Do not run `docker-compose` from this folder** — run it from the project's root directory (`library-system/`):

```bash
cd ../
docker-compose up -d
```

This starts a shared PostgreSQL instance, which includes `library_ai_db`.

---

## ☕ Step 2: Start the service

**Option A — simplest:** run the whole system via `docker-compose up -d` from the root (`ai-service` comes up automatically on port `9090`).

**Option B — run standalone from IntelliJ**, against the DB already running in Docker:

1. Update `application.properties` to match the shared DB (not the old `smart_library_db`/`library_user`/`library_password`):
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/library_ai_db
   spring.datasource.username=library_admin
   spring.datasource.password=UserService123
   ```
2. Run the service's main Application class.
3. The service will run on port `9090` (as set by `server.port`).

> ⚠️ **Groq API key:** `groq.api.key` is read from the `GROQ_API_KEY` environment variable — never hardcode a real key in `application.properties`. Set it in a local `.env` file (see `.env.example` at the project root) or as an Environment Variable in your IDE's Run Configuration / Docker.

---

## 🌐 Access

`http://localhost:9090/api/recommendations`

---

## 🛠️ Main Technologies

Java 21 · Spring Boot · Spring Data JPA · PostgreSQL · Groq API (openai/gpt-oss-20b) · Docker · Maven
