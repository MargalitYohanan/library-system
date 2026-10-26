# 👤 User & Auth Microservice

Part of the **Community Library Platform** microservices architecture.
This service is the **User & Auth Microservice**, responsible for user and neighborhood management, server-side security via JWT, and full Google OAuth2 integration.

---

## 🏗️ System Architecture

The system is composed of several core components, all brought up together via the `docker-compose.yml` at the **repository root**:

* **User Service (Backend)** — Spring Boot (Java 17/21), port **9000**
* **Book & Loaning Service** — port **8081**
* **Notification Service** — port **8082**
* **Smart Library AI** — port **9090**
* **Library UI (Frontend)** — React, port **3000**
* **Database (PostgreSQL)** — a single database shared by all services, running in a Docker container

---

## 🛠️ Prerequisites

- Java SDK 21
- Node.js 18+
- Docker Desktop

---

## 🚀 Step-by-Step Run Guide

### 1️⃣ Clone the project

```bash
git clone <your GitHub repository URL here>
cd library-system
```

### 2️⃣ Start the whole system (PostgreSQL + all services via Docker)

**Important:** run this from the project's **root** directory (`library-system/`), not from inside `userservice`:

```bash
docker-compose up -d
```

📌 This will:
* Start a shared PostgreSQL instance on port **5432**
* Automatically create all required databases, including `library_users_db`
* Bring up all the other services (Book, Notification, AI, UI)

### 3️⃣ Run the User Service separately (for development)

If you want to develop/debug just this service from your IDE (against the DB already running in Docker):

1. Open the `userservice` folder in your IDE (e.g. IntelliJ)
2. Make sure the connection details in `application.properties` match what the root `docker-compose` creates:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/library_users_db
   spring.datasource.username=library_admin
   spring.datasource.password=UserService123
   ```
3. Provide your own values for the environment variables below (see the security note) — either in a local `.env` file at the project root or as Environment Variables in your IDE's Run Configuration:
   ```
   GOOGLE_CLIENT_ID=...
   GOOGLE_CLIENT_SECRET=...
   JWT_SECRET=...
   ```
4. Run the `UserServiceApplication` class.

🌐 The server will run at: `http://localhost:9000`

### 4️⃣ Run the frontend (React UI) separately — optional

```bash
cd ../library-ui
npm install
npm start
```

🌐 The app will open at: `http://localhost:3000`

> ⚠️ **Security — Google OAuth & JWT secrets:** `client-id`, `client-secret`, and the JWT signing key (`jwt.secret`) are all read from environment variables (`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `JWT_SECRET`) — never write real values directly into `application.properties`. Create a Google OAuth client in the Google Cloud Console, and generate a long random string for `JWT_SECRET` (e.g. `openssl rand -base64 48`).

---

## 🔒 Security & Data Flows

### 🔑 1. User authentication with JWT
**Server side:** `JwtAuthenticationFilter`, `JwtTokenProvider` — every request to a protected route requires a valid token, signed with the secret from `JWT_SECRET`.
**Client side:** an Axios interceptor automatically attaches the token to every request.

### 🔐 2. Sign-in with Google OAuth2
The user is redirected to Google OAuth, and returns to the app after authentication:
* **Existing user** — signed in immediately; token, email, full name, and ID are stored.
* **New user (onboarding)** — redirected to the `CompleteGooglePage` to complete their phone number and choose a neighborhood.

### 🔄 3. Login state sync
Uses `useLocation` from `react-router-dom` to update the Navbar in real time, without a page refresh.

---

## 👨‍💻 Technologies Used

* **Backend:** Spring Boot, Spring Security, JWT
* **Frontend:** React, Axios
* **Database:** PostgreSQL
* **DevOps:** Docker, Docker Compose
* **Authentication:** Google OAuth2
