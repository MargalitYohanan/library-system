import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { getUnreadCount, getNotificationsPage, markAllAsRead } from '../../api/notificationApi';
import NotificationItem from '../../pages/NotificationPage/NotificationItem';
import './NotificationBell.css';

export default function NotificationBell() {
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasLoadedOnce, setHasLoadedOnce] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();
  const wrapperRef = useRef(null);

  const userId = localStorage.getItem('userId');

  // רענון מספר ההודעות שלא נקראו - בכל מעבר דף, כדי שהפעמון תמיד יהיה מעודכן
  useEffect(() => {
    if (!userId) return;
    getUnreadCount(userId)
      .then(setUnreadCount)
      .catch((err) => console.error('Error loading unread count:', err));
  }, [location, userId]);

  // סגירת הדרופדאון בלחיצה מחוץ לו
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const loadRecentNotifications = useCallback(() => {
    if (!userId) return;
    setLoading(true);
    getNotificationsPage(userId, 0, 5)
      .then((data) => {
        setNotifications(data.content || []);
        setHasLoadedOnce(true);
        // סימון כנקרא רק אחרי שבאמת הצגנו את הרשימה בפועל בדרופדאון
        if ((data.content || []).length > 0) {
          markAllAsRead(userId)
            .then(() => setUnreadCount(0))
            .catch((err) => console.error('Error marking read:', err));
        }
      })
      .catch((err) => console.error('Failed to load notifications:', err))
      .finally(() => setLoading(false));
  }, [userId]);

  const handleToggle = () => {
    const willOpen = !isOpen;
    setIsOpen(willOpen);
    if (willOpen) {
      loadRecentNotifications();
    }
  };

  const handleViewAll = () => {
    setIsOpen(false);
    navigate('/notifications');
  };

  return (
    <div className="notification-bell-wrapper" ref={wrapperRef}>
      <button
        type="button"
        className="notification-bell-trigger"
        onClick={handleToggle}
        aria-label="התראות"
        aria-expanded={isOpen}
      >
        <svg
          viewBox="0 0 24 24"
          width="22"
          height="22"
          fill="none"
          stroke="#2b2118"
          strokeWidth="1.8"
          aria-hidden="true"
        >
          <path
            d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
          <path
            d="M13.73 21a2 2 0 0 1-3.46 0"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
        {unreadCount > 0 && (
          <span className="notification-bell-badge">{unreadCount > 9 ? '9+' : unreadCount}</span>
        )}
      </button>

      {isOpen && (
        <div className="notification-dropdown">
          <div className="notification-dropdown__header">
            <span>התראות</span>
          </div>

          <div className="notification-dropdown__body">
            {loading && !hasLoadedOnce ? (
              <p className="notification-dropdown__status">טוען...</p>
            ) : notifications.length === 0 ? (
              <p className="notification-dropdown__status">אין לך התראות חדשות כרגע.</p>
            ) : (
              notifications.map((item) => (
                <NotificationItem key={item.id} notification={item} compact />
              ))
            )}
          </div>

          <button type="button" className="notification-dropdown__footer" onClick={handleViewAll}>
            לכל ההתראות
          </button>
        </div>
      )}
    </div>
  );
}
