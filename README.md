# 📚 Community Library Platform

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

> A neighborhood-based community library built with **Spring Boot microservices**, **React**, **PostgreSQL**, and **AI-powered recommendations**.

The platform lets neighbors register, manage their personal book collections, lend and borrow books locally, and receive personalized reading recommendations. A dedicated notification service keeps users informed by email.

---

## ✨ Highlights

- 👤 User registration, profiles, onboarding, and Google OAuth2 login
- 🏘️ Neighborhood-based community management
- 📖 Book catalog management and search
- 🔄 End-to-end lending and loan-request lifecycle
- 🤖 Personalized AI book recommendations using Groq
- 📧 Scheduled email notifications and reminders
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
| **notification-service** | `8082` | Scheduled and templated email notifications |
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

This supports scheduled messages such as loan reminders and system notifications.

---

## 🔐 Environment Variables

The project does **not** include real secrets. Sensitive configuration is supplied through environment variables.

Create a `.env` file based on `.env.example`, or configure the values through your IDE.

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
git clone <repository-url>
cd <repository-directory>
```

### 2. Configure environment variables

Create your `.env` file from the provided example:

```bash
cp .env.example .env
```

Then fill in the required values.

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
├── .env.example
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

