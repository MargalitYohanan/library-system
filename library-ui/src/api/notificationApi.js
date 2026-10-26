import { notificationApi as api } from "./axios";

// 1. שליפת כמות ההודעות שלא נקראו (עבור הפעמון)
export const getUnreadCount = async (userId) => {
  const { data } = await api.get(`/notifications/user/${userId}/unread-count`);
  return data; // יחזיר רק את המספר (למשל: 3)
};

// 2. שליפת הודעות בטעינה עצלה (דפים)
// הגדרנו ברירת מחדל של עמוד 0 וגודל דף 20, אבל הרכיב יוכל לשלוח מספרים אחרים
export const getNotificationsPage = async (userId, page = 0, size = 20) => {
  const { data } = await api.get(`/notifications/user/${userId}`, {
    params: {
      page: page,
      size: size,
    },
  });
  return data; 
};

// 3. סימון כל ההודעות כנקראו (איפוס גורף)
export const markAllAsRead = async (userId) => {
  const { data } = await api.put(`/notifications/user/${userId}/read`);
  return data; 
};