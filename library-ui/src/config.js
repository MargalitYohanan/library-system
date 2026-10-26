// src/config.js

// 1. AI Service (smart-library-ai) יושב בדוקר על פורט 9090
export const RECOMMENDATIONS_API_URL = process.env.REACT_APP_RECOMMENDATIONS_API_URL || "http://localhost:9090/api/recommendations";

// 2. Book & Loaning Service יושב בדוקר על פורט 8081
export const BOOKS_API_URL = process.env.REACT_APP_BOOKS_API_URL || "http://localhost:8081/api";

// 3. User Service יושב בדוקר על פורט 9000
export const USERS_API_URL = process.env.REACT_APP_USERS_API_URL || "http://localhost:9000/api";

// 4. Notification Service (email-service) יושב בדוקר על פורט 8082
export const NOTIFICATIONS_API_URL = process.env.REACT_APP_NOTIFICATIONS_API_URL || "http://localhost:8082/api";