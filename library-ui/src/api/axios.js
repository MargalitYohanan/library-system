import axios from "axios";
import { USERS_API_URL, BOOKS_API_URL, NOTIFICATIONS_API_URL, RECOMMENDATIONS_API_URL} from "../config";

const injectTokenInterceptor = (instance) => {
  instance.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });
  return instance;
};

// 1. מופע עבור User Service (פורט 9000)
export const userApi = injectTokenInterceptor(
  axios.create({ baseURL: USERS_API_URL, headers: { "Content-Type": "application/json" } })
);

// 2. מופע עבור Book & Loaning Service (פורט 8081)
export const bookAndLoanApi = injectTokenInterceptor(
  axios.create({ baseURL: BOOKS_API_URL, headers: { "Content-Type": "application/json" } })
);

// 3. מופע עבור Notification Service (פורט 8082)
export const notificationApi = injectTokenInterceptor(
  axios.create({ baseURL: NOTIFICATIONS_API_URL, headers: { "Content-Type": "application/json" } })
);

export const aiApi = injectTokenInterceptor(
  axios.create({ baseURL: RECOMMENDATIONS_API_URL, headers: { "Content-Type": "application/json" } })
);