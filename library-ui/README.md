# ⚛️ Library UI

The React frontend for the **Community Library Platform**. Consumes the `user-service`, `book-service`, `notification-service`, and `ai-service` REST APIs.

---

## 📋 Prerequisites

- Node.js 18+
- npm
- The backend services running (via `docker-compose` from the project root, or individually)

---

## 🚀 Running the app

**Option A — as part of the full system (recommended):**

From the project **root**:

```bash
docker-compose up -d
```

The UI comes up automatically on port `3000`.

**Option B — standalone, against already-running backend services:**

```bash
cd library-ui
npm install
npm start
```

The app opens at `http://localhost:3000`.

---

## ⚙️ Configuration

API base URLs are read from environment variables, with sensible localhost defaults for local development (see `src/config.js`):

| Variable | Default | Purpose |
|---|---|---|
| `REACT_APP_USERS_API_URL` | `http://localhost:9000/api` | User Service |
| `REACT_APP_BOOKS_API_URL` | `http://localhost:8081/api` | Book & Loaning Service |
| `REACT_APP_NOTIFICATIONS_API_URL` | `http://localhost:8082/api` | Notification Service |
| `REACT_APP_RECOMMENDATIONS_API_URL` | `http://localhost:9090/api/recommendations` | AI Service |

To override them, create a `.env` file inside `library-ui/` with the values relevant to your setup.

---

## 🛠️ Main Technologies

React 19 · React Router v7 · Axios
