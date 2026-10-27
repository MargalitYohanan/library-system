<div align="center">

# 📚 Community Library Platform

**Share books with your neighbors — powered by microservices, React and AI.**

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?logo=springsecurity&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![React Router](https://img.shields.io/badge/React%20Router-v7-CA4245?logo=reactrouter&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT-000000?logo=jsonwebtokens&logoColor=white)
![Google OAuth2](https://img.shields.io/badge/OAuth2-Google-4285F4?logo=google&logoColor=white)
![Groq](https://img.shields.io/badge/AI-Groq%20API-F55036?logo=lightning&logoColor=white)

</div>

> A neighborhood-based community library built with **Spring Boot microservices**, **React**, **PostgreSQL**, and **AI-powered recommendations**.

The platform lets neighbors register, manage their personal book collections, lend and borrow books locally, and receive personalized reading recommendations. A dedicated **Notification Service** keeps users informed by email.

> 👥 **Team project.** This system was built as a practicum project by several student teams, each owning one or more microservices. My part was the **Notification & Email Service** (`notification-service`), which is covered in detail in [its own section](#-spotlight-notification-service).

---

## 📑 Table of Contents

- [Highlights](#-highlights)
- [Architecture](#%EF%B8%8F-architecture)
- [Tech Stack](#-tech-stack)
- [Key Features](#-key-features)
- [⭐ Spotlight: Notification Service](#-spotlight-notification-service)
- [Environment Variables](#-environment-variables)
- [Getting Started](#-getting-started)
- [Service Endpoints](#-service-endpoints)
- [Running a Service Locally](#-running-a-service-locally)
- [Project Structure](#-project-structure)
- [How the Main Flow Works](#-how-the-main-flow-works)
- [Design Principles](#-design-principles)
- [Quick Reference](#-quick-reference)

---

## ✨ Highlights

- 👤 User registration, profiles, onboarding, and Google OAuth2 login
- 🏘️ Neighborhood-based community management
- 📖 Book catalog management and search
- 🔄 End-to-end lending and loan-request lifecycle
- 🤖 Personalized AI book recommendations using Groq
- 📧 **Reliable email notifications: instant, scheduled, retried on failure, and configurable per user**
- 🔗 HTTP-based communication between independent microservices
- 🐳 Docker Compose orchestration for the complete application
- ⚛️ React single-page frontend

---

## 🏗️ Architecture

The application is composed of **four independent Spring Boot microservices**, a **React frontend**, and a shared PostgreSQL instance with a separate database/schema responsibility per service.

```text
                              ┌────────────────────┐
                              │     library-ui     │
                              │     React :3000    │
                              └─────────┬──────────┘
                                        │ HTTP
              ┌─────────────────────────┼─────────────────────────┐
              │                         │                         │
              ▼                         ▼                         ▼
      ┌───────────────┐       ┌────────────────┐       ┌────────────────────┐
      │ user-service  │◀─────▶│  book-service  │──────▶│ notification-service│
      │    :9000      │       │ :8081 → :8080  │       │  :8082 → :8080     │
      └───────┬───────┘       └───────┬────────┘       └──────────┬─────────┘
              │                        │                           │
              └────────────────────────┼───────────────────────────┘
                                       ▼
                              ┌──────────────────┐
                              │    PostgreSQL    │
                              │      :5432       │
                              │ shared instance  │
                              └──────────────────┘

                              ┌──────────────────┐
                              │    ai-service    │
                              │      :9090       │
                              └────────┬─────────┘
                                       │
                              Calls user/book services
                              + external Groq AI API
```

### Service map

| Service | Port | Responsibility |
|---|---:|---|
| **user-service** | `9000` | Registration/login, Google OAuth2, profiles, onboarding, and neighborhoods |
| **book-service** | `8081` | Book catalog, search, ownership, borrowing, and loan requests |
| **notification-service** ⭐ | `8082` | Scheduled and templated email notifications |
| **ai-service** | `9090` | Personalized recommendations using an external Groq AI API |
| **library-ui** | `3000` | React frontend consuming the backend services |

### Service dependencies

From `docker-compose.yml`:

- `book-service` → PostgreSQL, `user-service`, `notification-service`
- `ai-service` → PostgreSQL, `user-service`, `book-service`
- `library-ui` → `user-service`, `book-service`, `notification-service`

---

## 🧰 Tech Stack

### Backend

- **Java 17+**
- **Spring Boot**
- Spring Web
- Spring Data JPA
- Spring Security
- OAuth2 Client
- Spring Mail
- Spring Quartz
- Thymeleaf
- Spring WebFlux / WebClient
- Maven + Maven Wrapper

### Frontend

- **React 19**
- **React Router v7**
- **Axios**

### Database & Infrastructure

- **PostgreSQL**
- **Docker**
- **Docker Compose**

### AI

- **Groq API**
- Dedicated `ai-service` for recommendation generation

---

## 🚀 Key Features

### 👤 User & Neighborhood Management

Users can:

- Register and log in
- Sign in with Google OAuth2
- Manage their profiles
- Complete the onboarding flow
- Join a neighborhood
- Share books locally with other users

### 📚 Book Catalog & Lending

The book service supports:

- Add books
- Edit books
- Remove books
- Search books
- Track owned books
- Track borrowed books
- Request a loan
- Manage pending loan requests

### 🤖 AI-Powered Recommendations

The `ai-service` generates personalized recommendations for users by:

1. Collecting relevant user information
2. Retrieving book information from the book service
3. Calling the external Groq LLM API
4. Generating personalized recommendations
5. Storing the recommendations per user

### 📬 Automated Notifications

The notification service uses:

- Spring Mail
- Spring Quartz
- Thymeleaf email templates

It sends instant emails (welcome, loan requests, approvals, password reset) and scheduled ones (return reminders, monthly digest), saves every message before sending, and retries failures automatically. See the full breakdown in the next section. 👇

---

## ⭐ Spotlight: Notification Service

> The communication channel of the whole platform: it connects community members in real time, nudges them to act (approve a request, return a book), and keeps engagement high.

The Notification Service is an **independent microservice** with its own database (`library_notifications_db`, hosted on the shared PostgreSQL instance). It is fully **decoupled** from inventory and user logic: other services only ask it to send a message. Its database provides an **audit log**, **spam protection (rate limiting)**, and a **retry mechanism** for when the mail server is down.

### 🎯 Goals

| Goal | What it means |
|---|---|
| ✅ Reliability | Every critical action is confirmed to the recipient; no message is lost to a temporary network failure |
| 🤖 Automation | Reminders replace manual tracking of return dates |
| 🛡️ Resilience | Messages are stored before sending, so failed ones can be retried |
| 🚫 Spam protection | Rate limiting guards users from duplicate or excessive messages |
| 🎛️ User control | Each user chooses which notifications to receive |

### 🧱 Internal Layers

| Layer | Role |
|---|---|
| **Controller** | Exposes internal endpoints that other microservices call to request a message |
| **Service & Scheduler** | Builds messages from templates, replaces placeholders, runs scheduled jobs, manages retries |
| **Repository** | Spring Data JPA; persists every message and its exact status |

```text
 Other services ──► Controller ──► Service ──► Template engine (Thymeleaf) ──► SMTP (Gmail)
 (book, user …)                       │
                                      ├──► Repository ──► PostgreSQL (audit log + status)
                                      │
 Quartz scheduler ───────────────────►┘  (reminders · monthly digest · retry job)
```

### 📧 Email Scenarios

**⚡ Instant (triggered by API calls from other services)**

| # | Email | Trigger | Purpose |
|---|---|---|---|
| 1 | 👋 Welcome & onboarding | New account created | Confirm sign-up and encourage adding a first book |
| 2 | 📥 New loan request | A user clicks "Request loan" on someone's book | Alert the owner of a pending request |
| 3 | ✅ Request approved | Owner approves the request | Tell the borrower the book is ready and share pickup details |
| 4 | ⏳ Loan extension request | Borrower asks for more time | Ask the owner to approve or decline, with quick-action buttons |
| 5 | 🔔 Book available again | A previously borrowed book is returned | Notify users on the wishlist/queue |
| 6 | 🔐 Password reset / change confirmation | Password reset requested or changed | Send a secure, time-limited link or a security confirmation |
| 9 | 🆗 Loan extension approved | Owner approves an extension | Confirm the new return date |

**⏰ Scheduled (run automatically in the background)**

| # | Email | Schedule | Purpose |
|---|---|---|---|
| 7 | 📅 Friendly return reminder | Daily, for books due in 3 days | Prevent late returns; includes an option to request an extension |
| 8 | 📊 Monthly digest | 1st of every month | Community stats (books lent, new members), personal stats, and estimated community savings |

### 🗄️ Data Model

Five tables support flexible, dynamic templates:

| Table | Purpose |
|---|---|
| `User_notification` | Local, partial copy of user contact data: id, name, email, phone, and a `Preferences` bitmask |
| `CONTENT` | Email templates: subject and HTML body with placeholders such as `{{BookName}}` |
| `NOTIFICATION` | One row per notification event: sender, recipient, `Status`, template, send date, `RetryCount`, `RequiredSendDate` (for scheduled sends) |
| `TYPE_ADDITIONAL_DETAILS` | Dictionary of variable types: `BookName`, `DueDate`, `ResetLink`, … |
| `NOTIFICATION_DETAILS_LINK` | Bridge table attaching any number of actual variable values to a notification |

### 🧩 How a Message Is Built

1. Load the template text from `CONTENT`.
2. Load the variable values for this notification from `NOTIFICATION_DETAILS_LINK`.
3. A parse step replaces every placeholder with its real value.
4. Thymeleaf renders the result into a final, responsive HTML email (readable on desktop and mobile).

### 🔁 Reliability: Store First, Then Send, Then Retry

```text
Request ─► save as PENDING ─► try to send ─┬─► success ─► SENT
                                           └─► failure ─► FAILED
                                                           │
            Retry job (every 15 min) ◄─────────────────────┘
            picks FAILED messages from the last 24h, max 3 attempts
```

- Every message is persisted with status `PENDING` **before** any send attempt.
- A scheduled **retry job** runs every 15 minutes and re-sends `FAILED` messages from the last 24 hours, up to **3 attempts** (tracked by `RetryCount`).
- **Rate limiting** protects users from duplicate or excessive messages.

### 🎛️ User Preferences (Bitmask)

Users pick which notifications they want through checkboxes in the UI. The choice is stored as a single integer bitmask, which keeps the eligibility check fast and cheap on the database.

- Each notification type maps to a bit (e.g. `LoanRequest = 1`, `ReturnReminder = 2`).
- Before sending: `UserPreferences & NotificationType`. A result greater than 0 means the message is sent.
- **Mandatory** messages (such as password reset) are defined in code and bypass the check.
- Default for new users: all bits on.

### 🔌 API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/notifications/send` | Receives a template (content) id, sender and recipient ids, and an array of dynamic variables. Creates the records and attempts to send immediately |
| `GET` | `/api/notifications/user/{userId}` | Returns the user's 20 most recent notifications, for the frontend's notification bell |

The endpoints are intended for internal service-to-service use and the admin interface, and can be explored through **Swagger UI (OpenAPI)** or Postman.

### 🚨 Error Handling

All exceptions are caught centrally with `@RestControllerAdvice` and returned in one consistent JSON format:

```json
{
  "errorCode": "...",
  "message": "...",
  "status": 400,
  "timestamp": "..."
}
```

Clients get a stable `errorCode` to react to, business logic stays separate from error handling, and internal details are never exposed.

### 🛠️ Notification Service Tech

Spring Boot · Spring Web · Spring Data JPA · **Spring Mail** (SMTP, e.g. Gmail) · **Quartz / Scheduler** · **Thymeleaf** · PostgreSQL · Swagger UI

---

## 🔐 Environment Variables

The project does **not** include real secrets. Sensitive configuration is supplied through environment variables.

Create a `.env` file in the project root with the variables below, or configure the values through your IDE.

| Variable | Service | Purpose |
|---|---|---|
| `GROQ_API_KEY` | `ai-service` | API key for the Groq LLM API |
| `GOOGLE_CLIENT_ID` | `user-service` | OAuth2 client ID for Google login |
| `GOOGLE_CLIENT_SECRET` | `user-service` | OAuth2 client secret for Google login |
| `JWT_SECRET` | `user-service` | Signing key for issued JWT auth tokens |
| `MAIL_USERNAME` | `notification-service` | Gmail account used to send notifications |
| `MAIL_APP_PASSWORD` | `notification-service` | Gmail App Password |

> ⚠️ **Security:** Never commit real API keys, OAuth secrets, passwords, or other credentials to Git.

---

## 🐳 Getting Started

### Prerequisites

Install:

- Docker & Docker Compose
- Java 17+ and Maven *(only required for running services outside Docker)*
- Node.js + npm *(only required for running the frontend outside Docker)*

### 1. Clone the repository

```bash
git clone https://github.com/MargalitYohanan/library-system.git
cd library-system
```

### 2. Configure environment variables

Create a `.env` file in the repository root and fill in the values listed in the [Environment Variables](#-environment-variables) table.

### 3. Start the complete system

From the repository root:

```bash
docker-compose up -d
```

This starts PostgreSQL, all backend services, and the React frontend on the shared Docker network.

### 4. Check running containers

```bash
docker-compose ps
```

### 5. Stop the system

```bash
docker-compose down
```

---

## 🌐 Service Endpoints

| Component | Host Port |
|---|---:|
| PostgreSQL | `5432` |
| User Service | `9000` |
| Book Service | `8081` |
| Notification Service | `8082` |
| AI Service | `9090` |
| React UI | `3000` |

---

## 🧪 Running a Service Locally

Each microservice contains its own:

- `README.md`
- `Dockerfile`
- `pom.xml`
- Maven Wrapper

A service can be started individually with:

```bash
./mvnw spring-boot:run
```

When running outside Docker, make sure the required PostgreSQL instance and environment variables are configured.

---

## 📁 Project Structure

A simplified repository structure looks like:

```text
library-system/
├── userservice/
├── bookandloaningservice/
├── notification-service/
├── smart-library-ai/
├── library-ui/
├── docker-compose.yml
├── init-multiple-databases.sql
├── .gitignore
└── README.md
```

---

## 🔄 How the Main Flow Works

```text
User
 │
 ▼
React Frontend
 │
 ├──────────────► User Service
 │                    │
 │                    └──► PostgreSQL
 │
 ├──────────────► Book Service
 │                    │
 │                    └──► PostgreSQL
 │
 ├──────────────► Notification Service
 │                    │
 │                    └──► Email
 │
 └──────────────► AI Service
                      │
                      ├──► User Service
                      ├──► Book Service
                      └──► Groq API
```

This separation keeps each business domain independent while allowing the services to collaborate through HTTP APIs.

---

## 🧩 Design Principles

The project follows a microservices-oriented design:

- **Domain separation** — each service owns a focused business responsibility.
- **Independent services** — services can be developed and run independently.
- **REST/HTTP communication** — services communicate through HTTP APIs.
- **Configuration through environment variables** — secrets stay outside source control.
- **Containerized deployment** — Docker provides a consistent runtime environment.

---

## 📝 Project Notes

- Each microservice has its own build configuration and Dockerfile.
- PostgreSQL runs as a shared instance while service data remains logically separated.
- The frontend communicates with the backend services through HTTP.
- The AI functionality is isolated in its own service.
- Notification scheduling is isolated from the core lending logic.

---

## 📌 Quick Reference

```text
Frontend        → React 19
Routing         → React Router v7
HTTP Client     → Axios

Backend         → Java + Spring Boot
Persistence     → Spring Data JPA
Security        → Spring Security + OAuth2
Email           → Spring Mail + Thymeleaf
Scheduling      → Spring Quartz
AI Client       → Spring WebFlux / WebClient

Database        → PostgreSQL
Containers      → Docker + Docker Compose
AI Provider     → Groq
```

---

## ⚠️ Important

Before starting the application, make sure all required environment variables are configured and that **no real secrets are committed to the repository**.
