import React, { useState, useEffect } from 'react';
import { getNotificationsPage, markAllAsRead } from '../../api/notificationApi'; 
import NotificationItem from './NotificationItem';
import './NotificationPage.css';

export default function NotificationPage() {
  const [notifications, setNotifications] = useState([]); 
  const [page, setPage] = useState(0);                    
  const [hasMore, setHasMore] = useState(true);           
  const [loading, setLoading] = useState(false);          

  const userId = localStorage.getItem("userId");

  // פונקציה מרכזית לטעינת דף הודעות ספציפי
  const loadNotificationsData = (pageNumber) => {
    if (!userId) return;
    setLoading(true);

    getNotificationsPage(userId, pageNumber, 20)
      .then((data) => {
        const newItems = data.content || [];

        setNotifications((prev) => [...prev, ...newItems]);

        setHasMore(!data.last);
        setLoading(false);

        if (pageNumber === 0 && newItems.length > 0) {
          markAllAsRead(userId).catch((err) => console.error("Error marking read:", err));
        }
      })
      .catch((err) => {
        console.error('Failed to load notifications:', err);
        setLoading(false);
      });
  };

 useEffect(() => {
    loadNotificationsData(0);
  }, []);

  const handleLoadMore = () => {
    const nextPage = page + 1;
    setPage(nextPage);             
    loadNotificationsData(nextPage);
  };

  return (
    <div className="notification-page">
      <h2 className="notification-page__title">התראות</h2>

      {/* רשימת ההודעות */}
      <div className="notification-page__list">
        {notifications.length === 0 && !loading ? (
          <p className="notification-page__empty">אין לך הודעות או התראות במערכת.</p>
        ) : (
          notifications.map((item) => (
            <NotificationItem key={item.id} notification={item} />
          ))
        )}
      </div>

      {loading && <p className="notification-page__loading">טוען הודעות נוספות...</p>}

      {hasMore && !loading && notifications.length > 0 && (
        <button
          className="notification-page__load-more"
          onClick={handleLoadMore}
        >
          טען הודעות נוספות
        </button>
      )}
    </div>
  );
}
